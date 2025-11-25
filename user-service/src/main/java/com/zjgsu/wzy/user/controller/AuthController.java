package com.zjgsu.wzy.user.controller;

import com.zjgsu.wzy.user.dto.LoginRequest;
import com.zjgsu.wzy.user.dto.LoginResponse;
import com.zjgsu.wzy.user.model.User;
import com.zjgsu.wzy.user.service.UserService;
import com.zjgsu.wzy.user.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("登录请求: username={}", request.getUsername());

        // 1. 验证用户名和密码
        User user = userService.findByUsername(request.getUsername());
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            log.warn("登录失败: 用户名或密码错误, username={}", request.getUsername());
            Map<String, String> error = new HashMap<>();
            error.put("error", "用户名或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // 2. 生成 JWT Token
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole());

        // 3. 返回 Token 和用户信息
        log.info("登录成功: username={}, userId={}, role={}", user.getUsername(), user.getId(), user.getRole());
        return ResponseEntity.ok(new LoginResponse(token, user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        log.info("注册请求: username={}", user.getUsername());

        if (userService.existsByUsername(user.getUsername())) {
            log.warn("注册失败: 用户名已存在, username={}", user.getUsername());
            Map<String, String> error = new HashMap<>();
            error.put("error", "用户名已存在");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        User savedUser = userService.save(user);
        log.info("注册成功: username={}, userId={}", savedUser.getUsername(), savedUser.getId());

        // 生成Token并返回
        String token = jwtUtil.generateToken(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole());

        return ResponseEntity.ok(new LoginResponse(token, savedUser));
    }
}
