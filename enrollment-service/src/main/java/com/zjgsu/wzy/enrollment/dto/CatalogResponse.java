package com.zjgsu.wzy.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogResponse {
    private boolean success;
    private Map<String, Object> message;
}
