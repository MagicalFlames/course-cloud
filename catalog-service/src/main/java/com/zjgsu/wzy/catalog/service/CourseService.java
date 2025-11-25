package com.zjgsu.wzy.catalog.service;

import com.zjgsu.wzy.catalog.model.Course;
import com.zjgsu.wzy.catalog.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {
    private final CourseRepository courseRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Course> findById(String id) {
        return courseRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Course> findAll(){
        return courseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Course> findByCode(String code) {
        return courseRepository.findByCode(code);
    }

    public Course add(Course course){
        if (courseRepository.existsByCode(course.getCode())) {
            throw new IllegalArgumentException("课程代码已存在: " + course.getCode());
        }
        return courseRepository.save(course);
    }

    public boolean delete(String id){
        Optional<Course> course = courseRepository.findById(id);
        if(course.isEmpty()){
            return false;
        }
        courseRepository.delete(course.get());
        return true;
    }

    public Course update(String id, Course courseDetails){
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在: " + id));

        if (!course.getCode().equals(courseDetails.getCode()) &&
            courseRepository.existsByCode(courseDetails.getCode())) {
            throw new IllegalArgumentException("课程代码已存在: " + courseDetails.getCode());
        }

        course.setCode(courseDetails.getCode());
        course.setTitle(courseDetails.getTitle());
        course.setInstructor(courseDetails.getInstructor());
        course.setScheduleSlot(courseDetails.getScheduleSlot());
        course.setCapacity(courseDetails.getCapacity());
        if (courseDetails.getEnrolledCount() != null) {
            course.setEnrolledCount(courseDetails.getEnrolledCount());
        }

        return courseRepository.save(course);
    }
}
