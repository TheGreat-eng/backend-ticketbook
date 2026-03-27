package com.example.App.auth;

import com.example.App.auth.dto.AuthResponse;
import com.example.App.auth.dto.LoginRequest;
import com.example.App.auth.dto.RegisterRequest;
import com.example.App.repository.RoleRepository;
import com.example.App.repository.UserRepository;
import com.example.App.security.JwtUtils;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.App.entity.Role;
import com.example.App.entity.User;  
import static com.example.App.entity.Role.RoleName;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    
    // Khai báo thêm 2 cái này để dùng tạm cho việc fix pass
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    private final RoleRepository roleRepository;

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





@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
    // 1. Kiểm tra email tồn tại
    if (userRepository.existsByEmail(registerRequest.getEmail())) {
        return ResponseEntity.badRequest().body("Email này đã được sử dụng!");
    }

    // 2. Tạo User mới và mã hóa mật khẩu
    User user = new User();
    user.setEmail(registerRequest.getEmail());
    user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

    // 3. Gán Role mặc định (ROLE_USER)
    Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
            .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy Role mặc định."));
    user.setRoles(Set.of(userRole));

    userRepository.save(user);

    return ResponseEntity.ok("Đăng ký tài khoản thành công!");
}
}