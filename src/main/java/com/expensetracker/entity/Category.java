package com.expensetracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Category Entity
 * 
 * Represents a category for transactions (e.g., "Food", "Transport", "Salary")
 * 
 * Categories can be:
 * - Default (shared by all users) - user_id is NULL
 * - User-specific (created by user) - user_id is set
 * 
 * Types:
 * - INCOME: Categories for income transactions
 * - EXPENSE: Categories for expense transactions
 */
@Entity
@Table(
    name = "categories",
    indexes = {
        @Index(name = "idx_category_user_type", columnList = "user_id,type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this category
     * NULL = Default category (available to all users)
     * NOT NULL = User-specific category
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    /**
     * Category name (e.g., "Food", "Transport", "Salary")
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Transaction type this category belongs to
     * INCOME - for income transactions
     * EXPENSE - for expense transactions
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    /**
     * Icon name (optional) - for UI display
     * e.g., "food", "car", "salary"
     */
    @Column(length = 50)
    private String icon;

    /**
     * Color code (optional) - for UI display
     * e.g., "#FF5733"
     */
    @Column(length = 7)
    private String color;

    /**
     * Is this a default category?
     * Default categories are pre-created and available to all users
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
     * Transaction Type Enum
     */
    public enum TransactionType {
        INCOME,
        EXPENSE
    }
}

