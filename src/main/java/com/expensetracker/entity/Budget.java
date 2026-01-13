package com.expensetracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Budget Entity
 * 
 * Represents a budget set by the user for a specific period
 * 
 * Budgets can be:
 * - Category-specific: Budget for a specific category (e.g., "Food: $500/month")
 * - Overall: Total budget (category_id is NULL, e.g., "Total expenses: $2000/month")
 * 
 * Period Types:
 * - MONTHLY: Budget resets every month
 * - YEARLY: Budget resets every year
 * - CUSTOM: Budget for a specific date range
 */
@Entity
@Table(
    name = "budgets",
    indexes = {
        @Index(name = "idx_budget_user_dates", columnList = "user_id,start_date,end_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_budget_user_category_start",
            columnNames = {"user_id", "category_id", "start_date"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this budget
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Category this budget is for
     * NULL = Overall budget (total expenses/income)
     * NOT NULL = Category-specific budget
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    /**
     * Budget amount
     * The maximum amount allowed for this period
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Period type
     * MONTHLY - Budget resets monthly
     * YEARLY - Budget resets yearly
     * CUSTOM - Budget for specific date range
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private PeriodType periodType;

    /**
     * Start date of the budget period
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * End date of the budget period
     * NULL for MONTHLY/YEARLY (calculated automatically)
     * Required for CUSTOM period type
     */
    @Column(name = "end_date", nullable = true)
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // For MONTHLY budgets, set end date to end of month
        if (periodType == PeriodType.MONTHLY && endDate == null) {
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }
        
        // For YEARLY budgets, set end date to end of year
        if (periodType == PeriodType.YEARLY && endDate == null) {
            endDate = startDate.withDayOfYear(startDate.lengthOfYear());
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if budget is currently active
     * 
     * @return true if current date is between start and end date
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && (endDate == null || !today.isAfter(endDate));
    }

    /**
     * Period Type Enum
     */
    public enum PeriodType {
        MONTHLY,
        YEARLY,
        CUSTOM
    }
}

