package com.zjgsu.wzy.enrollment.service;

import com.zjgsu.wzy.enrollment.model.Student;
import com.zjgsu.wzy.enrollment.repository.EnrollmentRepository;
import com.zjgsu.wzy.enrollment.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public Student create(Student student) {
        if (studentRepository.existsByStudentId(student.getStudentId())) {
            throw new IllegalArgumentException("学号已存在: " + student.getStudentId());
        }

        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在: " + student.getEmail());
        }

        return studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Student> findById(String id) {
        return studentRepository.findById(id);
    }

    public Student update(String id, Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在: " + id));

        if (!student.getStudentId().equals(studentDetails.getStudentId()) &&
            studentRepository.existsByStudentId(studentDetails.getStudentId())) {
            throw new IllegalArgumentException("学号已存在: " + studentDetails.getStudentId());
        }

        if (!student.getEmail().equals(studentDetails.getEmail()) &&
            studentRepository.existsByEmail(studentDetails.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在: " + studentDetails.getEmail());
        }

        student.setStudentId(studentDetails.getStudentId());
        student.setName(studentDetails.getName());
        student.setMajor(studentDetails.getMajor());
        student.setGrade(studentDetails.getGrade());
        student.setEmail(studentDetails.getEmail());

        return studentRepository.save(student);
    }

    public boolean delete(String id) {
        Optional<Student> studentOpt = studentRepository.findById(id);
        if (studentOpt.isEmpty()) {
            return false;
        }

        long activeEnrollments = enrollmentRepository.findByStudentId(id).stream()
                .filter(e -> e.getStatus() == com.zjgsu.wzy.enrollment.model.EnrollmentStatus.ACTIVE)
                .count();

        if (activeEnrollments > 0) {
            throw new IllegalStateException("无法删除学生，存在 " + activeEnrollments + " 个活跃选课记录");
        }

        studentRepository.delete(studentOpt.get());
        return true;
    }
}
