package com.example.App.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.App.dto.ApiResponse;
import com.example.App.entity.Booking;
import com.example.App.repository.BookingRepository;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {


    // src/main/java/com/example/App/controller/UserController.java

private final BookingRepository bookingRepository; // Inject cái này vào constructor

@GetMapping("/me/bookings")
public ResponseEntity<ApiResponse<List<Booking>>> getMyBookings(Authentication authentication) {
    if (authentication == null) return ResponseEntity.status(401).build();
    
    List<Booking> myBookings = bookingRepository.findByUserEmail(authentication.getName());
    return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", myBookings));
}

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