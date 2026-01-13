package com.expensetracker.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Token Provider
 * 
 * This class handles:
 * 1. Generating JWT tokens
 * 2. Validating JWT tokens
 * 3. Extracting information from tokens
 * 
 * What is JWT?
 * - JSON Web Token - a secure way to transmit information
 * - Contains: Header, Payload (claims), Signature
 * - Stateless - server doesn't need to store sessions
 * 
 * Structure:
 * Header.Payload.Signature
 * 
 * Example:
 * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
 */
@Component
public class JwtTokenProvider {

    /**
     * Secret key for signing tokens
     * In production, use environment variable!
     * 
     * @Value - Injects value from application.properties
     */
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration; // in milliseconds (15 minutes = 900000)

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration; // in milliseconds (7 days = 604800000)

    /**
     * Get the signing key
     * Converts secret string to SecretKey for HMAC-SHA256
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate Access Token
     * 
     * Access tokens are short-lived (15 minutes)
     * Used for API requests
     * 
     * @param userDetails - User information
     * @return JWT token string
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return createToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    /**
     * Generate Refresh Token
     * 
     * Refresh tokens are long-lived (7 days)
     * Used to get new access tokens without re-login
     * 
     * @param userDetails - User information
     * @return JWT token string
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    /**
     * Create JWT Token
     * 
     * Steps:
     * 1. Set claims (data to store in token)
     * 2. Set subject (usually username/email)
     * 3. Set issued time
     * 4. Set expiration time
     * 5. Sign with secret key
     * 
     * @param claims - Additional data to store
     * @param subject - Subject (username/email)
     * @param expiration - Expiration time in milliseconds
     * @return JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims) // Custom claims
                .subject(subject) // Subject (email in our case)
                .issuedAt(now) // When token was issued
                .expiration(expiryDate) // When token expires
                .signWith(getSigningKey()) // Sign with secret key
                .compact(); // Build the token
    }

    /**
     * Extract username from token
     * 
     * @param token - JWT token
     * @return username (email)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from token
     * 
     * @param token - JWT token
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim from token
     * 
     * Generic method to extract any claim
     * 
     * @param token - JWT token
     * @param claimsResolver - Function to extract specific claim
     * @return Claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     * 
     * This parses the token and extracts all data
     * 
     * @param token - JWT token
     * @return Claims object containing all token data
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // Verify signature
                .build()
                .parseSignedClaims(token) // Parse token
                .getPayload(); // Get claims
    }

    /**
     * Check if token is expired
     * 
     * @param token - JWT token
     * @return true if expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token
     * 
     * Checks:
     * 1. Token is not expired
     * 2. Username matches
     * 
     * @param token - JWT token
     * @param userDetails - User details to validate against
     * @return true if valid, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Validate token without UserDetails
     * 
     * Used for refresh token validation
     * 
     * @param token - JWT token
     * @return true if valid, false otherwise
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}

