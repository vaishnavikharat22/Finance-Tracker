package com.expensetracker.config;

import com.expensetracker.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration
 * 
 * This is the MAIN security configuration for Spring Security.
 * 
 * What does it configure?
 * 1. Password encoding (BCrypt)
 * 2. Authentication provider (how to authenticate users)
 * 3. Security filter chain (which endpoints are public/protected)
 * 4. CORS (Cross-Origin Resource Sharing)
 * 5. JWT filter (when to validate tokens)
 * 
 * @Configuration - Marks this as a configuration class
 * @EnableWebSecurity - Enables Spring Security
 * @EnableMethodSecurity - Enables @PreAuthorize, @Secured annotations (for future use)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Security Filter Chain
     * 
     * This defines:
     * - Which endpoints are public (no auth required)
     * - Which endpoints are protected (auth required)
     * - How to handle authentication
     * - CORS configuration
     * 
     * @param http - HttpSecurity builder
     * @return SecurityFilterChain
     * @throws Exception - If configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (Cross-Site Request Forgery)
            // We're using JWT tokens, so CSRF protection is not needed
            .csrf(AbstractHttpConfigurer::disable)

            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no authentication required)
                .requestMatchers(
                    "/api/v1/auth/**",           // All auth endpoints
                    "/swagger-ui/**",            // Swagger UI
                    "/api-docs/**",              // API documentation
                    "/v3/api-docs/**"            // OpenAPI docs
                ).permitAll()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // Session management
            // STATELESS = Don't create HTTP sessions (we use JWT tokens)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Authentication provider
            .authenticationProvider(authenticationProvider())

            // Add JWT filter BEFORE UsernamePasswordAuthenticationFilter
            // This ensures JWT tokens are validated before other filters
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Authentication Provider
     * 
     * Tells Spring Security HOW to authenticate users:
     * 1. Load user by username (email)
     * 2. Compare password with stored hash
     * 
     * @return DaoAuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Password Encoder
     * 
     * BCrypt is a strong password hashing algorithm.
     * 
     * Why BCrypt?
     * - One-way hashing (can't reverse it)
     * - Salted (each hash is unique)
     * - Slow (prevents brute force attacks)
     * - Industry standard
     * 
     * Strength 10 = 2^10 iterations (good balance of security and performance)
     * 
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Authentication Manager
     * 
     * Used by Spring Security to authenticate users.
     * Required for login endpoint.
     * 
     * @param config - AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception - If configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS Configuration
     * 
     * CORS (Cross-Origin Resource Sharing) allows browsers to make requests
     * from one domain (frontend) to another (backend).
     * 
     * Example:
     * - Frontend: http://localhost:3000 (React)
     * - Backend: http://localhost:8080 (Spring Boot)
     * - Without CORS, browser blocks the request
     * 
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow these origins (frontend URLs)
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",  // React default
            "http://localhost:4200"   // Angular default
        ));
        
        // Allow these HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow these headers
        configuration.setAllowedHeaders(List.of("*"));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}

