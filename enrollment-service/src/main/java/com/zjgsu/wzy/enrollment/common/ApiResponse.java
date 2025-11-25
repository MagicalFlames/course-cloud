package com.zjgsu.wzy.enrollment.common;

import java.util.Map;

public class ApiResponse {

    private boolean success;
    private Map<String, Object> message;

    public ApiResponse(boolean success, Map<String, Object> message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Map<String, Object> getMessage() {
        return message;
    }

    public void setMessage(Map<String, Object> message) {
        this.message= message;
    }
}
