package com.expensetracker.repository;

import com.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository
 * 
 * This interface extends JpaRepository, which provides:
 * - save(), findById(), findAll(), delete(), etc.
 * - No need to write SQL queries for basic operations!
 * 
 * Spring Data JPA automatically creates implementations at runtime.
 * 
 * @Repository - Marks this as a Spring Data repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     * 
     * Spring Data JPA automatically generates the query:
     * SELECT * FROM users WHERE email = ?
     * 
     * Naming convention: findBy + FieldName
     * 
     * @param email - user's email
     * @return Optional<User> - Optional prevents NullPointerException
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email exists
     * 
     * Generated query: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     * 
     * @param email - email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
}

