package com.example.App.auth;

import com.example.App.auth.dto.AuthResponse;
import com.example.App.auth.dto.LoginRequest;
import com.example.App.repository.UserRepository;
import com.example.App.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.App.entity.User;  
import com.example.App.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    
    // Khai báo thêm 2 cái này để dùng tạm cho việc fix pass
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ----- API TẠM THỜI ĐỂ SỬA LỖI MẬT KHẨU -----
    @GetMapping("/fix-pass")
    public String fixPassword() {
        User user = userRepository.findByEmail("test@gmail.com").orElse(null);
        if (user != null) {
            // Nhờ chính Spring mã hoá và lưu lại
            user.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(user);
            return "Đã cập nhật mật khẩu chuẩn BCrypt cho test@gmail.com!";
        }
        return "Không tìm thấy user trong DB";
    }
    // ---------------------------------------------


@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    // IN RA ĐỂ KIỂM TRA XEM SPRING CÓ LẤY ĐƯỢC DATA TỪ POSTMAN KHÔNG
    System.out.println("Email nhận được: " + loginRequest.getEmail());
    System.out.println("Pass nhận được: " + loginRequest.getPassword());

    try {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), 
                        loginRequest.getPassword()
                )
        );

        String token = jwtUtils.generateToken(
                authentication.getName(), 
                authentication.getAuthorities()
        );
        return ResponseEntity.ok(new AuthResponse(token));

    } catch (Exception e) {
        // IN LỖI THẬT RA CONSOLE ĐỂ BẮT BỆNH
        e.printStackTrace(); 
        return ResponseEntity.status(401).body("Email hoặc mật khẩu không chính xác: " + e.getMessage());
    }
}
}