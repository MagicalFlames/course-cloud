package com.zjgsu.wzy.enrollment.service;

import com.zjgsu.wzy.enrollment.exception.ResourceNotFoundException;
import com.zjgsu.wzy.enrollment.exception.BusinessException;
import com.zjgsu.wzy.enrollment.model.Enrollment;
import com.zjgsu.wzy.enrollment.model.EnrollmentStatus;
import com.zjgsu.wzy.enrollment.model.Student;
import com.zjgsu.wzy.enrollment.repository.EnrollmentRepository;
import com.zjgsu.wzy.enrollment.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    @Value("${catalog-service.url}")
    private String catalogServiceUrl;

    private static final String CATALOG_SERVICE_NAME = "catalog-service";

    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                            StudentRepository studentRepository,
                            RestTemplate restTemplate,
                            DiscoveryClient discoveryClient) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    /**
     * 从 Nacos 获取 catalog-service 的 URL
     * 如果 Nacos 不可用，回退到配置的 URL
     */
    private String getCatalogServiceUrl() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(CATALOG_SERVICE_NAME);
            if (!instances.isEmpty()) {
                ServiceInstance instance = instances.get(0);
                return instance.getUri().toString();
            }
        } catch (Exception e) {
            System.err.println("Failed to get service from Nacos, falling back to configured URL: " + e.getMessage());
        }
        return catalogServiceUrl;
    }

    public Enrollment enroll(String courseId, String studentId) {
        // 1. 验证学生是否存在
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        // 2. 调用课程目录服务验证课程是否存在
        String url = getCatalogServiceUrl() + "/api/courses/" + courseId;
        Map<String, Object> courseResponse;
        try {
            courseResponse = restTemplate.getForObject(url, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Course", courseId);
        }

        // 3. 从响应中提取课程信息
        Map<String, Object> message = (Map<String, Object>) courseResponse.get("message");
        Map<String, Object> courseData = (Map<String, Object>) message.get("data");
        Integer capacity = (Integer) courseData.get("capacity");
        Integer enrolled = (Integer) courseData.get("enrolledCount");

        // 4. 检查课程容量
        if (enrolled >= capacity) {
            throw new BusinessException("Course is full");
        }

        // 5. 检查重复选课
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new BusinessException("Already enrolled in this course");
        }

        // 6. 创建选课记录
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        Enrollment saved = enrollmentRepository.save(enrollment);

        // 7. 更新课程的已选人数（调用catalog-service）
        updateCourseEnrolledCount(courseId, enrolled + 1);

        return saved;
    }

    private void updateCourseEnrolledCount(String courseId, int newCount) {
        String url = getCatalogServiceUrl() + "/api/courses/" + courseId;
        Map<String, Object> updateData = Map.of("enrolledCount", newCount);
        try {
            restTemplate.put(url, updateData);
        } catch (Exception e) {
            System.err.println("Failed to update course enrolled count: " + e.getMessage());
        }
    }

    public boolean drop(String enrollmentId) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
        if (enrollmentOpt.isEmpty()) {
            return false;
        }

        Enrollment enrollment = enrollmentOpt.get();

        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new IllegalStateException("该选课记录不是活跃状态，无法退课");
        }

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);

        // 获取课程当前的已选人数并更新
        String url = getCatalogServiceUrl() + "/api/courses/" + enrollment.getCourseId();
        try {
            Map<String, Object> courseResponse = restTemplate.getForObject(url, Map.class);
            Map<String, Object> message = (Map<String, Object>) courseResponse.get("message");
            Map<String, Object> courseData = (Map<String, Object>) message.get("data");
            Integer currentEnrolled = (Integer) courseData.get("enrolledCount");
            updateCourseEnrolledCount(enrollment.getCourseId(), Math.max(0, currentEnrolled - 1));
        } catch (Exception e) {
            System.err.println("Failed to update course enrolled count after drop: " + e.getMessage());
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
