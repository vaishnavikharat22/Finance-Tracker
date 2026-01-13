package com.expensetracker.repository;

import com.expensetracker.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Transaction Repository
 * 
 * Spring Data JPA repository for Transaction entity
 * 
 * Why Pageable?
 * - For pagination (don't load all transactions at once)
 * - Better performance for large datasets
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions for a user (paginated)
     * 
     * @param userId - User ID
     * @param pageable - Pagination parameters (page, size, sort)
     * @return Page of transactions
     */
    Page<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable);

    /**
     * Find transactions by user and date range
     * 
     * @param userId - User ID
     * @param startDate - Start date (inclusive)
     * @param endDate - End date (inclusive)
     * @param pageable - Pagination parameters
     * @return Page of transactions
     */
    Page<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    /**
     * Find transactions by user and type
     * 
     * @param userId - User ID
     * @param type - Transaction type (INCOME/EXPENSE)
     * @param pageable - Pagination parameters
     * @return Page of transactions
     */
    Page<Transaction> findByUserIdAndTypeOrderByTransactionDateDesc(
            Long userId,
            Transaction.TransactionType type,
            Pageable pageable
    );

    /**
     * Find transactions by user, category, and date range
     * 
     * @param userId - User ID
     * @param categoryId - Category ID
     * @param startDate - Start date
     * @param endDate - End date
     * @return List of transactions
     */
    List<Transaction> findByUserIdAndCategoryIdAndTransactionDateBetween(
            Long userId,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * Calculate total amount for user in date range by type
     * 
     * Used for reports and summaries
     * 
     * @param userId - User ID
     * @param type - Transaction type
     * @param startDate - Start date
     * @param endDate - End date
     * @return Total amount (sum of all transactions)
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE " +
           "t.user.id = :userId AND t.type = :type AND " +
           "t.transactionDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumAmountByUserIdAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") Transaction.TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate total amount by category for user in date range
     * 
     * Used for category-wise spending reports
     * 
     * @param userId - User ID
     * @param startDate - Start date
     * @param endDate - End date
     * @return List of [categoryId, categoryName, totalAmount]
     */
    @Query("SELECT t.category.id, t.category.name, SUM(t.amount) " +
           "FROM Transaction t WHERE " +
           "t.user.id = :userId AND t.type = 'EXPENSE' AND " +
           "t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category.id, t.category.name " +
           "ORDER BY SUM(t.amount) DESC")
    List<Object[]> sumAmountByCategoryForUser(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

