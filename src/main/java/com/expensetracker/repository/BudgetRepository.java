package com.expensetracker.repository;

import com.expensetracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Budget Repository
 * 
 * Spring Data JPA repository for Budget entity
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Find all budgets for a user
     * 
     * @param userId - User ID
     * @return List of budgets
     */
    List<Budget> findByUserIdOrderByStartDateDesc(Long userId);

    /**
     * Find active budgets for a user
     * Active = current date is between start and end date
     * 
     * @param userId - User ID
     * @param currentDate - Current date
     * @return List of active budgets
     */
    @Query("SELECT b FROM Budget b WHERE " +
           "b.user.id = :userId AND " +
           "b.startDate <= :currentDate AND " +
           "(b.endDate IS NULL OR b.endDate >= :currentDate) " +
           "ORDER BY b.startDate DESC")
    List<Budget> findActiveBudgetsByUserId(
            @Param("userId") Long userId,
            @Param("currentDate") LocalDate currentDate
    );

    /**
     * Find budget for user, category, and date
     * 
     * Used to check if budget exists for a specific category and period
     * 
     * @param userId - User ID
     * @param categoryId - Category ID (can be null for overall budget)
     * @param date - Date to check
     * @return Optional Budget
     */
    @Query("SELECT b FROM Budget b WHERE " +
           "b.user.id = :userId AND " +
           "(:categoryId IS NULL AND b.category IS NULL OR b.category.id = :categoryId) AND " +
           "b.startDate <= :date AND " +
           "(b.endDate IS NULL OR b.endDate >= :date)")
    Optional<Budget> findBudgetForUserAndCategoryAndDate(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("date") LocalDate date
    );

    /**
     * Find budgets for user in date range
     * 
     * @param userId - User ID
     * @param startDate - Start date
     * @param endDate - End date
     * @return List of budgets
     */
    @Query("SELECT b FROM Budget b WHERE " +
           "b.user.id = :userId AND " +
           "((b.startDate BETWEEN :startDate AND :endDate) OR " +
           "(b.endDate BETWEEN :startDate AND :endDate) OR " +
           "(b.startDate <= :startDate AND (b.endDate IS NULL OR b.endDate >= :endDate)))")
    List<Budget> findBudgetsInDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

