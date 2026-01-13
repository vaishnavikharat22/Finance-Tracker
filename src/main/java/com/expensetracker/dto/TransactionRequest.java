package com.expensetracker.dto;

import com.expensetracker.entity.Transaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Transaction Request DTO
 * 
 * Used for creating and updating transactions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Type is required")
    private Transaction.TransactionType type;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private LocalDate transactionDate; // Optional, defaults to today if not provided
}

