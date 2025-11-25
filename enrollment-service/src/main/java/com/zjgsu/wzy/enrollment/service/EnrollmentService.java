package com.zjgsu.wzy.enrollment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjgsu.wzy.enrollment.client.CatalogClient;
import com.zjgsu.wzy.enrollment.dto.CatalogResponse;
import com.zjgsu.wzy.enrollment.exception.ResourceNotFoundException;
import com.zjgsu.wzy.enrollment.exception.BusinessException;
import com.zjgsu.wzy.enrollment.model.Enrollment;
import com.zjgsu.wzy.enrollment.model.EnrollmentStatus;
import com.zjgsu.wzy.enrollment.model.Student;
import com.zjgsu.wzy.enrollment.repository.EnrollmentRepository;
import com.zjgsu.wzy.enrollment.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CatalogClient catalogClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                            StudentRepository studentRepository,
                            CatalogClient catalogClient,
                            ObjectMapper objectMapper) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.catalogClient = catalogClient;
        this.objectMapper = objectMapper;
    }

    public Enrollment enroll(String courseId, String studentId) {
        // 1. 验证学生是否存在
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        // 2. 通过 Feign Client 调用课程目录服务验证课程是否存在
        log.info("Calling catalog service to get course: {}", courseId);
        CatalogResponse courseResponse = catalogClient.getCourse(courseId);

        if (!courseResponse.isSuccess()) {
            log.error("Course not found: {}", courseId);
            throw new ResourceNotFoundException("Course", courseId);
        }

        // 3. 从响应中提取课程信息
        Map<String, Object> message = courseResponse.getMessage();
        Map<String, Object> courseData = (Map<String, Object>) message.get("data");

        // 转换 capacity 和 enrolledCount - 可能是 Integer 或 Long
        Number capacityNum = (Number) courseData.get("capacity");
        Number enrolledNum = (Number) courseData.get("enrolledCount");
        long capacity = capacityNum.longValue();
        long enrolled = enrolledNum.longValue();

        log.info("Course {} capacity: {}, enrolled: {}", courseId, capacity, enrolled);

        // 4. 检查课程容量
        if (enrolled >= capacity) {
            log.warn("Course {} is full (capacity: {}, enrolled: {})", courseId, capacity, enrolled);
            throw new BusinessException("Course is full");
        }

        // 5. 检查重复选课
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            log.warn("Student {} already enrolled in course {}", studentId, courseId);
            throw new BusinessException("Already enrolled in this course");
        }

        // 6. 创建选课记录
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Created enrollment record for student {} in course {}", studentId, courseId);

        // 7. 更新课程的已选人数（调用catalog-service）
        updateCourseEnrolledCount(courseId, enrolled + 1);

        return saved;
    }

    private void updateCourseEnrolledCount(String courseId, long newCount) {
        Map<String, Object> updateData = Map.of("enrolledCount", newCount);
        try {
            log.info("Updating course {} enrolled count to {}", courseId, newCount);
            catalogClient.updateCourse(courseId, updateData);
        } catch (Exception e) {
            log.error("Failed to update course enrolled count: {}", e.getMessage(), e);
        }
    }

    public boolean drop(String enrollmentId) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
        if (enrollmentOpt.isEmpty()) {
            log.warn("Enrollment not found: {}", enrollmentId);
            return false;
        }

        Enrollment enrollment = enrollmentOpt.get();

        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            log.error("Enrollment {} is not active, cannot drop", enrollmentId);
            throw new IllegalStateException("该选课记录不是活跃状态，无法退课");
        }

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);
        log.info("Dropped enrollment {} for student {} from course {}",
                enrollmentId, enrollment.getStudentId(), enrollment.getCourseId());

        // 获取课程当前的已选人数并更新
        try {
            CatalogResponse courseResponse = catalogClient.getCourse(enrollment.getCourseId());
            if (courseResponse.isSuccess()) {
                Map<String, Object> message = courseResponse.getMessage();
                Map<String, Object> courseData = (Map<String, Object>) message.get("data");
                Number currentEnrolledNum = (Number) courseData.get("enrolledCount");
                long currentEnrolled = currentEnrolledNum.longValue();
                updateCourseEnrolledCount(enrollment.getCourseId(), Math.max(0, currentEnrolled - 1));
            }
        } catch (Exception e) {
            log.error("Failed to update course enrolled count after drop: {}", e.getMessage(), e);
        }

        return true;
    }

    @Transactional(readOnly = true)
    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Enrollment> findByCourseId(String courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> findByStudentId(String studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }
}
