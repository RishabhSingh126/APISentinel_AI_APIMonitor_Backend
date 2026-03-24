package com.portfolio.apimonitor.controller;

import com.portfolio.apimonitor.dto.AuthResponse;
import com.portfolio.apimonitor.dto.LoginRequest;
import com.portfolio.apimonitor.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // This maps all routes in this class to start with /api/auth
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Requirement: POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody LoginRequest request) {
        // Hands the request to our AuthService, and wraps the result in an HTTP 200 OK response
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Requirement: POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // Hands the request to our AuthService, and wraps the result in an HTTP 200 OK response
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}