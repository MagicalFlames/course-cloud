package com.zjgsu.wzy.catalog.controller;

import com.zjgsu.wzy.catalog.common.ApiResponse;
import com.zjgsu.wzy.catalog.model.Course;
import com.zjgsu.wzy.catalog.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "课程管理", description = "课程目录服务的课程管理接口")
public class CourseController {
    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @Operation(summary = "获取所有课程", description = "返回所有课程的列表")
    public ApiResponse getAllCourses() {
        List<Course> courses = courseService.findAll();
        return new ApiResponse(true, Map.of(
                "ok","课程获取成功",
                "data", courses
        ));
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "根据ID获取课程", description = "通过课程ID获取课程详细信息")
    public ApiResponse getCourseById(
            @Parameter(description = "课程ID", required = true) @PathVariable String courseId) {
        Optional<Course> course = courseService.findById(courseId);
        if(course.isPresent()) {
            return new ApiResponse(true, Map.of(
                    "ok","查询成功",
                    "data", course.get()
            ));
        }
        else{
            return new ApiResponse(false, Map.of(
                    "error","课程不存在"
            ));
        }
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据课程代码获取课程", description = "通过课程代码（如CS101）获取课程信息")
    public ApiResponse getCourseByCode(
            @Parameter(description = "课程代码", required = true, example = "CS101") @PathVariable String code) {
        Optional<Course> course = courseService.findByCode(code);
        if(course.isPresent()) {
            return new ApiResponse(true, Map.of(
                    "ok","查询成功",
                    "data", course.get()
            ));
        }
        else{
            return new ApiResponse(false, Map.of(
                    "error","课程不存在"
            ));
        }
    }

    @PostMapping
    @Operation(summary = "创建新课程", description = "添加一个新的课程到目录中")
    public ApiResponse addCourse(
            @Parameter(description = "课程信息", required = true) @RequestBody Course course) {
        try {
            Course savedCourse = courseService.add(course);
            return new ApiResponse(true, Map.of(
                    "ok", "课程添加成功",
                    "data", savedCourse
            ));
        } catch (IllegalArgumentException e) {
            return new ApiResponse(false, Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{courseId}")
    @Operation(summary = "更新课程", description = "更新指定课程的信息")
    public ApiResponse updateCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable String courseId,
            @Parameter(description = "更新的课程信息", required = true) @RequestBody Course updatedCourse) {
        try {
            Course course = courseService.update(courseId, updatedCourse);
            return new ApiResponse(true, Map.of(
                    "ok", "课程更新成功",
                    "data", course
            ));
        } catch (IllegalArgumentException e) {
            return new ApiResponse(false, Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "删除课程", description = "从目录中删除指定的课程")
    public ApiResponse deleteCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable String courseId) {
        if (courseService.delete(courseId)) {
            return new ApiResponse(true, Map.of(
                    "ok", "课程删除成功"
            ));
        } else {
            return new ApiResponse(false, Map.of(
                    "error", "课程不存在"
            ));
        }
    }
}
