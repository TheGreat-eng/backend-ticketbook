package com.example.App.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(Authentication authentication) {
        // Nếu token hợp lệ, Spring sẽ tự nạp thông tin vào đối tượng authentication
        if (authentication == null) {
            return ResponseEntity.status(401).body("Bạn chưa đăng nhập hoặc Token không hợp lệ!");
        }

        // Trả về một Map chứa thông tin để Frontend hiển thị
        return ResponseEntity.ok(Map.of(
            "username", authentication.getName(),
            "authorities", authentication.getAuthorities(),
            "message", "Chào mừng bạn đến với khu vực VIP!"
        ));
    }
}