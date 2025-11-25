package com.zjgsu.wzy.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("Received user info from gateway: userId={}, username={}, role={}", userId, username, role);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Authentication successful");
        response.put("userId", userId);
        response.put("username", username);
        response.put("role", role);

        return ResponseEntity.ok(response);
    }
}
