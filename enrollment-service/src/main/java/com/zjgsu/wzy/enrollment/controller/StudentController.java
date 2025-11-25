package com.zjgsu.wzy.enrollment.controller;

import com.zjgsu.wzy.enrollment.common.ApiResponse;
import com.zjgsu.wzy.enrollment.model.Student;
import com.zjgsu.wzy.enrollment.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@Tag(name = "学生管理", description = "学生信息管理接口")
public class StudentController {
    private StudentService studentService;

    @Autowired
    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @Operation(summary = "创建学生", description = "添加一个新的学生到系统")
    public ApiResponse createStudent(
            @Parameter(description = "学生信息", required = true) @RequestBody Student student) {
        try {
            Student savedStudent = studentService.create(student);
            return new ApiResponse(true, Map.of(
                    "ok", "创建成功",
                    "data", savedStudent
            ));
        } catch (IllegalArgumentException e) {
            return new ApiResponse(false, Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping
    @Operation(summary = "获取所有学生", description = "返回系统中所有学生的列表")
    public ApiResponse getAllStudents() {
        return new ApiResponse(true,Map.of(
                "ok","获取成功",
                "data",studentService.findAll()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取学生", description = "通过学生ID获取学生详细信息")
    public ApiResponse getStudentById(
            @Parameter(description = "学生ID", required = true) @PathVariable String id) {
        Optional<Student> studentOpt = studentService.findById(id);
        return studentOpt.map(student -> new ApiResponse(true, Map.of(
                "ok", "获取成功",
                "data", student
        ))).orElseGet(() -> new ApiResponse(false, Map.of(
                "error", "学生不存在"
        )));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新学生信息", description = "更新指定学生的信息")
    public ApiResponse updateStudent(
            @Parameter(description = "学生ID", required = true) @PathVariable String id,
            @Parameter(description = "更新的学生信息", required = true) @RequestBody Student updatedStudent) {
        try {
            Student student = studentService.update(id, updatedStudent);
            return new ApiResponse(true, Map.of(
                    "ok", "学生信息更新成功",
                    "data", student
            ));
        } catch (IllegalArgumentException e) {
            return new ApiResponse(false, Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除学生", description = "从系统中删除指定的学生")
    public ApiResponse deleteStudent(
            @Parameter(description = "学生ID", required = true) @PathVariable String id) {
        try {
            if (studentService.delete(id)) {
                return new ApiResponse(true, Map.of(
                        "ok", "学生删除成功"
                ));
            } else {
                return new ApiResponse(false, Map.of(
                        "error", "学生不存在"
                ));
            }
        } catch (IllegalStateException e) {
            return new ApiResponse(false, Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}
