package com.example.App.auth.dto;
import lombok.Data;
import java.util.Set;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    // Bạn có thể thêm các trường khác như fullName, phone...
}