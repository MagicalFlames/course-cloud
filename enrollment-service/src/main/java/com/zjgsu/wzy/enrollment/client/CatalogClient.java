package com.zjgsu.wzy.enrollment.client;

import com.zjgsu.wzy.enrollment.dto.CatalogResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
    name = "catalog-service",
    fallback = CatalogClientFallback.class
)
public interface CatalogClient {

    @GetMapping("/api/courses/{id}")
    CatalogResponse getCourse(@PathVariable("id") String id);

    @PutMapping("/api/courses/{id}")
    CatalogResponse updateCourse(@PathVariable("id") String id, @RequestBody Map<String, Object> updateData);
}
