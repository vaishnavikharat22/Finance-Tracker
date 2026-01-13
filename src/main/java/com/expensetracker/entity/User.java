package com.expensetracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * User Entity
 * 
 * This represents a user in our system.
 * 
 * Why implement UserDetails?
 * - Spring Security needs UserDetails to authenticate users
 * - It provides methods like getPassword(), getAuthorities(), etc.
 * - This is the standard way to integrate with Spring Security
 * 
 * @Entity - Marks this as a JPA entity (will be stored in database)
 * @Table - Specifies the table name and indexes for performance
 */
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true)
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    /**
     * Primary Key
     * @GeneratedValue(strategy = GenerationType.IDENTITY) - Auto-increment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email - must be unique
     * Used for login
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Password - stored as hash (BCrypt)
     * Never store plain text passwords!
     */
    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * Account enabled flag
     * Can be used for account activation/deactivation
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * Timestamps - automatically managed by JPA
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA Lifecycle Callbacks
     * These methods run automatically before save/update
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * UserDetails Interface Methods
     * Required by Spring Security
     */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For now, all users have ROLE_USER
        // Later, you can add roles like ROLE_ADMIN
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        // Spring Security uses email as username
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Can implement account expiration logic later
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Can implement account locking logic later
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Can implement password expiration logic later
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

