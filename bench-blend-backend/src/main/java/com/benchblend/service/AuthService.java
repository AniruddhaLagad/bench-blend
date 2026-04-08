package com.benchblend.service;

import com.benchblend.config.JwtConfig;
import com.benchblend.dto.AuthResponse;
import com.benchblend.dto.LoginRequest;
import com.benchblend.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;
    private final AdminUserRepository adminUserRepository;

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        String token = jwtConfig.generateToken(request.getUsername());

        var admin = adminUserRepository.findByUsername(request.getUsername()).orElseThrow();

        return AuthResponse.builder()
                .token(token)
                .username(admin.getUsername())
                .email(admin.getEmail())
                .expiresIn(jwtConfig.getExpirationMs())
                .build();
    }
}