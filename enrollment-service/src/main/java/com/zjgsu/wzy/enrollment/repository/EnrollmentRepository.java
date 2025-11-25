package com.zjgsu.wzy.enrollment.repository;

import com.zjgsu.wzy.enrollment.model.Enrollment;
import com.zjgsu.wzy.enrollment.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    List<Enrollment> findByCourseId(String courseId);

    List<Enrollment> findByStudentId(String studentId);

    Optional<Enrollment> findByStudentIdAndCourseId(String studentId, String courseId);

    List<Enrollment> findByStatus(EnrollmentStatus status);

    List<Enrollment> findByCourseIdAndStatus(String courseId, EnrollmentStatus status);

    List<Enrollment> findByStudentIdAndStatus(String studentId, EnrollmentStatus status);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseId = :courseId AND e.status = 'ACTIVE'")
    long countActiveByCourseId(@Param("courseId") String courseId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.studentId = :studentId AND e.courseId = :courseId AND e.status = 'ACTIVE'")
    boolean existsActiveEnrollment(@Param("studentId") String studentId, @Param("courseId") String courseId);

    boolean existsByCourseIdAndStudentId(String courseId, String studentId);
}
