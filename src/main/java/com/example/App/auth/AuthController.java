package com.example.App.auth;

import com.example.App.auth.dto.AuthResponse;
import com.example.App.auth.dto.LoginRequest;
import com.example.App.auth.dto.RegisterRequest;
import com.example.App.dto.ApiResponse; // IMPORT MỚI
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
import jakarta.validation.Valid; // IMPORT MỚI

import com.example.App.entity.Role;
import com.example.App.entity.User;
import static com.example.App.entity.Role.RoleName;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest loginRequest) {
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
            
            // Trả về JSON chuẩn
            return ResponseEntity.ok(new ApiResponse<>(200, "Đăng nhập thành công", new AuthResponse(token)));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                .body(new ApiResponse<>(401, "Email hoặc mật khẩu không chính xác!", null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, "Email này đã được sử dụng!", null));
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy Role mặc định."));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse<>(200, "Đăng ký tài khoản thành công!", null));
    }
}