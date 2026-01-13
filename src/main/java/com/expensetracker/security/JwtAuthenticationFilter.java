package com.expensetracker.security;

import com.expensetracker.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * 
 * This filter runs BEFORE every request to protected endpoints.
 * 
 * What does it do?
 * 1. Extract JWT token from Authorization header
 * 2. Validate the token
 * 3. If valid, set authentication in SecurityContext
 * 4. Allow request to proceed
 * 
 * Why OncePerRequestFilter?
 * - Ensures filter runs only once per request
 * - Prevents multiple executions
 * 
 * Filter Chain:
 * Request → JwtAuthenticationFilter → Controller
 *          (validates token)         (processes request)
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * This method is called for every HTTP request
     * 
     * Steps:
     * 1. Extract token from request
     * 2. Validate token
     * 3. Load user details
     * 4. Set authentication in SecurityContext
     * 5. Continue filter chain
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Step 1: Extract token from request
            String token = getTokenFromRequest(request);

            // Step 2: Validate token
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                // Step 3: Extract username from token
                String username = jwtTokenProvider.extractUsername(token);

                // Step 4: Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Step 5: Create authentication object
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // credentials (not needed after authentication)
                        userDetails.getAuthorities()
                );

                // Step 6: Set details (IP address, session ID, etc.)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 7: Set authentication in SecurityContext
                // This tells Spring Security: "This user is authenticated"
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Log error but don't fail the request
            // If token is invalid, SecurityContext remains empty
            // Spring Security will reject the request later
            logger.error("Cannot set user authentication: {}", e);
        }

        // Step 8: Continue to next filter/controller
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     * 
     * Format: "Authorization: Bearer <token>"
     * 
     * @param request - HTTP request
     * @return Token string or null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Remove "Bearer " prefix
            return bearerToken.substring(7);
        }

        return null;
    }
}

