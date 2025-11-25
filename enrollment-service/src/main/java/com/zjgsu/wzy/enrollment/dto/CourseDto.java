package com.zjgsu.wzy.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private String id;
    private String code;
    private String title;
    private Long capacity;
    private Long enrolledCount;
    private LocalDateTime createdAt;
}
