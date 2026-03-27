package com.example.App.exception;

import com.example.App.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Lỗi Validate Form (Đã làm ở Phase 1)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Dữ liệu không hợp lệ", errors));
    }

    // 2. Lỗi Sai Tài Khoản hoặc Mật Khẩu (Rất quan trọng cho form Login)
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiResponse<String>> handleAuthExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(401, "Tài khoản hoặc mật khẩu không chính xác!", null));
    }

    // 3. Lỗi chung chung (Bắt các lỗi bất ngờ như NullPointer, đứt kết nối DB...)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex) {
        // IN RA CONSOLE ĐỂ BACKEND DEV FIX LỖI
        ex.printStackTrace(); 
        
        // TRẢ VỀ FRONTEND DÒNG CHỮ LỊCH SỰ
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "Lỗi hệ thống, vui lòng thử lại sau!", null));
    }
}