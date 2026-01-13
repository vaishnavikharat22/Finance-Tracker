package com.expensetracker.controller;

import com.expensetracker.dto.AuthResponse;
import com.expensetracker.dto.LoginRequest;
import com.expensetracker.dto.RefreshTokenRequest;
import com.expensetracker.dto.RegisterRequest;
import com.expensetracker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * REST Controller for authentication endpoints
 * 
 * @RestController - Combines @Controller + @ResponseBody
 * @RequestMapping - Base path for all endpoints in this controller
 * 
 * Endpoints:
 * - POST /api/v1/auth/register - Register new user
 * - POST /api/v1/auth/login - Login user
 * - POST /api/v1/auth/refresh - Refresh access token
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register new user
     * 
     * @param request - Registration request (validated automatically)
     * @return AuthResponse with JWT tokens
     * 
     * @Valid - Triggers validation annotations in RegisterRequest
     * @PostMapping - Handles POST requests
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user
     * 
     * @param request - Login request (validated automatically)
     * @return AuthResponse with JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token
     * 
     * Used when access token expires (15 minutes)
     * Client sends refresh token, gets new access token
     * 
     * @param request - Refresh token request
     * @return AuthResponse with new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}

