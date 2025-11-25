package com.zjgsu.wzy.user.dto;

import com.zjgsu.wzy.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserInfo user;

    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = new UserInfo(user.getId(), user.getUsername(), user.getRole(), user.getEmail(), user.getRealName());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String username;
        private String role;
        private String email;
        private String realName;
    }
}
