package com.example.App.auth;

import com.example.App.auth.dto.AuthResponse;
import com.example.App.auth.dto.LoginRequest;
import com.example.App.auth.dto.RegisterRequest;
import com.example.App.auth.dto.TokenRefreshRequest;
import com.example.App.dto.ApiResponse;
import com.example.App.entity.RefreshToken;
import com.example.App.entity.Role;
import com.example.App.entity.User;
import com.example.App.repository.RoleRepository;
import com.example.App.repository.UserRepository;
import com.example.App.security.JwtUtils;
import com.example.App.security.RefreshTokenService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

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
    
    // Khai báo service Refresh Token
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    // THÊM `throws Exception` Ở ĐÂY ĐỂ FIX LỖI ẢNH 1
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest loginRequest) throws Exception { 
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // Tạo Access Token
        String jwt = jwtUtils.generateToken(
                authentication.getName(),
                authentication.getAuthorities()
        );

        // Lấy User ra để tạo Refresh Token
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // Xóa Refresh Token cũ của user này (nếu có) để không rác DB
        refreshTokenService.deleteByUserId(user.getId());

        // Tạo Refresh Token mới
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        // Trả về cả Access Token và Refresh Token
        return ResponseEntity.ok(new ApiResponse<>(200, "Đăng nhập thành công", 
                new AuthResponse(jwt, refreshToken.getToken())));
    }


    @PostMapping("/refresh")
    // THÊM `throws Exception` VÀ VIẾT LẠI LOGIC ĐỂ FIX LỖI ẢNH 2
    public ResponseEntity<ApiResponse<?>> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) throws Exception {
        String requestRefreshToken = request.getRefreshToken();

        // 1. Tìm Refresh Token trong Database
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại trong DB!"));

        // 2. Kiểm tra xem Token đã hết hạn chưa (nếu hết hạn, hàm này sẽ ném ra lỗi)
        refreshTokenService.verifyExpiration(refreshToken);

        // 3. Lấy thông tin User sở hữu token này
        User user = refreshToken.getUser();

        // 4. Lấy danh sách quyền (Roles) của User
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        // 5. Tạo Access Token MỚI tinh
        String newAccessToken = jwtUtils.generateToken(user.getEmail(), authorities);

        // 6. Trả về cho Frontend
        return ResponseEntity.ok(new ApiResponse<>(200, "Làm mới Token thành công", 
                new AuthResponse(newAccessToken, requestRefreshToken)));
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