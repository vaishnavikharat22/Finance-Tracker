package com.expensetracker.service.impl;

import com.expensetracker.dto.AuthResponse;
import com.expensetracker.dto.LoginRequest;
import com.expensetracker.dto.RegisterRequest;
import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.jwt.JwtTokenProvider;
import com.expensetracker.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service Implementation
 * 
 * This class handles:
 * 1. User registration
 * 2. User login
 * 3. Token refresh
 * 
 * @Service - Marks this as a Spring service (business logic layer)
 * @Transactional - Ensures database operations are atomic
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user
     * 
     * Steps:
     * 1. Check if email already exists
     * 2. Hash the password
     * 3. Create user entity
     * 4. Save to database
     * 5. Generate JWT tokens
     * 6. Return response
     * 
     * @param request - Registration request
     * @return AuthResponse with tokens
     */
    @Override
    public AuthResponse register(RegisterRequest request) {
        // Step 1: Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Step 2: Create user entity
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Hash password
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .build();

        // Step 3: Save to database
        user = userRepository.save(user);

        // Step 4: Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Step 5: Build response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L) // 15 minutes in seconds
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .build())
                .build();
    }

    /**
     * Login user
     * 
     * Steps:
     * 1. Authenticate user (Spring Security validates credentials)
     * 2. Generate JWT tokens
     * 3. Return response
     * 
     * @param request - Login request
     * @return AuthResponse with tokens
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        // Step 1: Authenticate user
        // Spring Security will:
        // - Load user from database
        // - Compare password with stored hash
        // - Throw exception if invalid
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Step 2: Get authenticated user
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 3: Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // Step 4: Build response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L) // 15 minutes in seconds
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .build())
                .build();
    }

    /**
     * Refresh access token
     * 
     * Steps:
     * 1. Validate refresh token
     * 2. Extract username from token
     * 3. Load user
     * 4. Generate new access token
     * 5. Return response
     * 
     * @param refreshToken - Refresh token string
     * @return AuthResponse with new access token
     */
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // Step 1: Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Step 2: Extract username from token
        String username = jwtTokenProvider.extractUsername(refreshToken);

        // Step 3: Load user
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 4: Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        // Step 5: Build response (return same refresh token)
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Return same refresh token
                .tokenType("Bearer")
                .expiresIn(900L) // 15 minutes in seconds
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .build())
                .build();
    }
}

