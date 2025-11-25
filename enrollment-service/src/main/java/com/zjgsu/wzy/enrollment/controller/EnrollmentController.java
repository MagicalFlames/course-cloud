package com.zjgsu.wzy.enrollment.controller;

import com.zjgsu.wzy.enrollment.common.ApiResponse;
import com.zjgsu.wzy.enrollment.model.Enrollment;
import com.zjgsu.wzy.enrollment.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@Tag(name = "选课管理", description = "学生选课管理接口，包含选课、退课等功能")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @Operation(summary = "学生选课", description = "学生选择课程，会调用课程服务验证课程存在性和容量")
    public ApiResponse enroll(
            @Parameter(description = "选课请求，包含 studentId 和 courseId", required = true)
            @RequestBody Map<String, String> request) {
        try {
            String studentId = request.get("studentId");
            String courseId = request.get("courseId");

            if (studentId == null || courseId == null) {
                return new ApiResponse(false, Map.of(
                        "error", "studentId 和 courseId 不能为空"
                ));
            }

            Enrollment enrollment = enrollmentService.enroll(courseId, studentId);
            return new ApiResponse(true, Map.of(
                    "ok", "选课成功",
                    "data", enrollment
            ));
        } catch (Exception e) {
            return new ApiResponse(false, Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "学生退课", description = "学生退出已选课程")
    public ApiResponse dropCourse(
            @Parameter(description = "选课记录ID", required = true) @PathVariable String id) {
        try {
            if (enrollmentService.drop(id)) {
                return new ApiResponse(true, Map.of(
                        "ok", "退课成功"
                ));
            } else {
                return new ApiResponse(false, Map.of(
                        "error", "选课记录不存在"
                ));
            }
        } catch (IllegalStateException e) {
            return new ApiResponse(false, Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping
    @Operation(summary = "获取所有选课记录", description = "返回系统中所有的选课记录")
    public ApiResponse getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentService.findAll();
        return new ApiResponse(true, Map.of(
                "ok","获取成功",
                "data", enrollments
        ));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "按课程查询选课记录", description = "获取指定课程的所有选课记录")
    public ApiResponse getEnrollmentsByCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable String courseId) {
        List<Enrollment> enrollments = enrollmentService.findByCourseId(courseId);
        return new ApiResponse(true, Map.of(
                "ok","获取成功",
                "data", enrollments
        ));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "按学生查询选课记录", description = "获取指定学生的所有选课记录")
    public ApiResponse getEnrollmentsByStudent(
            @Parameter(description = "学生ID", required = true) @PathVariable String studentId) {
        List<Enrollment> enrollments = enrollmentService.findByStudentId(studentId);
        return new ApiResponse(true, Map.of(
                "ok","查询成功",
                "data", enrollments
        ));
    }
}
