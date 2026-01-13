package com.expensetracker.repository;

import com.expensetracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Category Repository
 * 
 * Spring Data JPA repository for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find all categories for a user
     * Includes:
     * - User's custom categories
     * - Default categories (available to all users)
     * 
     * @param userId - User ID
     * @param type - Transaction type (INCOME/EXPENSE)
     * @return List of categories
     */
    @Query("SELECT c FROM Category c WHERE " +
           "(c.user.id = :userId OR c.isDefault = true) AND c.type = :type " +
           "ORDER BY c.isDefault ASC, c.name ASC")
    List<Category> findByUserIdAndType(@Param("userId") Long userId, 
                                         @Param("type") Category.TransactionType type);

    /**
     * Find all categories for a user (any type)
     * 
     * @param userId - User ID
     * @return List of categories
     */
    @Query("SELECT c FROM Category c WHERE " +
           "c.user.id = :userId OR c.isDefault = true " +
           "ORDER BY c.isDefault ASC, c.type ASC, c.name ASC")
    List<Category> findByUserId(@Param("userId") Long userId);

    /**
     * Find user's custom categories only (not default)
     * 
     * @param userId - User ID
     * @return List of user's custom categories
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.isDefault = false")
    List<Category> findCustomCategoriesByUserId(@Param("userId") Long userId);

    /**
     * Find default categories
     * 
     * @return List of default categories
     */
    List<Category> findByIsDefaultTrue();
}

