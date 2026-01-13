package com.expensetracker.security;

import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom User Details Service
 * 
 * This class implements UserDetailsService interface.
 * Spring Security uses this to load user information during authentication.
 * 
 * Why do we need this?
 * - Spring Security needs to know how to load users from database
 * - This bridges our User entity with Spring Security's UserDetails
 * 
 * Flow:
 * 1. User tries to login with email/password
 * 2. Spring Security calls loadUserByUsername(email)
 * 3. We fetch user from database
 * 4. Return UserDetails (our User entity implements this)
 * 5. Spring Security validates password
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username (email in our case)
     * 
     * This method is called by Spring Security during authentication.
     * 
     * @param email - User's email (used as username)
     * @return UserDetails - Our User entity implements this
     * @throws UsernameNotFoundException - If user not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Return UserDetails (our User entity implements UserDetails)
        return user;
    }
}

