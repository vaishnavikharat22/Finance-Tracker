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
 * Transaction Entity
 * 
 * Represents a financial transaction (income or expense)
 * 
 * This is the CORE entity of our application.
 * Every transaction belongs to:
 * - A user (who made the transaction)
 * - A category (what type of transaction)
 * 
 * Important fields:
 * - amount: Always positive, type determines if income/expense
 * - transactionDate: When the transaction occurred (not when it was recorded)
 * - description: Optional note about the transaction
 */
@Entity
@Table(
    name = "transactions",
    indexes = {
        @Index(name = "idx_transaction_user_date", columnList = "user_id,transaction_date"),
        @Index(name = "idx_transaction_user_type", columnList = "user_id,type"),
        @Index(name = "idx_transaction_category", columnList = "category_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this transaction
     * 
     * @ManyToOne - Many transactions belong to one user
     * @JoinColumn - Foreign key column name
     * LAZY loading - User is loaded only when accessed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Category this transaction belongs to
     * 
     * @ManyToOne - Many transactions belong to one category
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Transaction amount
     * Always positive - type determines if income/expense
     * 
     * DECIMAL(15,2) - Can store up to 999,999,999,999,999.99
     * BigDecimal - Use for money (avoids floating point errors)
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Transaction type
     * INCOME - Money coming in (salary, bonus, etc.)
     * EXPENSE - Money going out (food, rent, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    /**
     * Optional description/note about the transaction
     * e.g., "Lunch at restaurant", "Monthly salary"
     */
    @Column(length = 500)
    private String description;

    /**
     * Date when the transaction occurred
     * Not when it was recorded in the system
     * 
     * Used for filtering and reports
     */
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // If transactionDate is not set, use today's date
        if (transactionDate == null) {
            transactionDate = LocalDate.now();
        }
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

