package com.zjgsu.wzy.enrollment.repository;

import com.zjgsu.wzy.enrollment.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {

    Optional<Student> findByStudentId(String studentId);

    Optional<Student> findByEmail(String email);

    boolean existsByStudentId(String studentId);

    boolean existsByEmail(String email);

    List<Student> findByMajor(String major);

    List<Student> findByGrade(Long grade);
}
