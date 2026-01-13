package com.expensetracker.dto;

import com.expensetracker.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Transaction Filter DTO
 * 
 * Used for filtering transactions in GET requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionFilter {

    private Transaction.TransactionType type;
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 20;
    @Builder.Default
    private String sortBy = "transactionDate";
    @Builder.Default
    private String sortDir = "desc";
}

