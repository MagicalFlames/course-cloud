package com.zjgsu.wzy.catalog.repository;

import com.zjgsu.wzy.catalog.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, String> {

    Optional<Course> findByCode(String code);

    @Query("SELECT c FROM Course c WHERE c.instructor.id = :instructorId")
    List<Course> findByInstructorId(@Param("instructorId") String instructorId);

    @Query("SELECT c FROM Course c WHERE c.enrolledCount < c.capacity")
    List<Course> findCoursesWithAvailableSeats();

    List<Course> findByTitleContaining(String keyword);

    boolean existsByCode(String code);
}
