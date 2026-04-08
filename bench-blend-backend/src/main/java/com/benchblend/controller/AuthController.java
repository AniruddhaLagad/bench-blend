package com.benchblend.controller;

import com.benchblend.dto.ApiResponse;
import com.benchblend.dto.AuthResponse;
import com.benchblend.dto.LoginRequest;
import com.benchblend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/hash")
    public String hash() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("admin@2003");
    }
}