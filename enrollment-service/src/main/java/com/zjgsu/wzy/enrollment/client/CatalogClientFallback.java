package com.zjgsu.wzy.enrollment.client;

import com.zjgsu.wzy.enrollment.dto.CatalogResponse;
import com.zjgsu.wzy.enrollment.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class CatalogClientFallback implements CatalogClient {

    @Override
    public CatalogResponse getCourse(String id) {
        log.warn("CatalogClient fallback triggered for course: {}", id);
        throw new ServiceUnavailableException("课程目录服务暂时不可用，请稍后再试");
    }

    @Override
    public CatalogResponse updateCourse(String id, Map<String, Object> updateData) {
        log.warn("CatalogClient fallback triggered for course update: {}", id);
        throw new ServiceUnavailableException("课程目录服务暂时不可用，请稍后再试");
    }
}
