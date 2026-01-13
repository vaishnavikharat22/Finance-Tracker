package com.expensetracker.service;

import com.expensetracker.dto.TransactionFilter;
import com.expensetracker.dto.TransactionRequest;
import com.expensetracker.dto.TransactionResponse;
import org.springframework.data.domain.Page;

/**
 * Transaction Service Interface
 */
public interface TransactionService {
    TransactionResponse createTransaction(Long userId, TransactionRequest request);
    TransactionResponse getTransactionById(Long userId, Long transactionId);
    Page<TransactionResponse> getTransactions(Long userId, TransactionFilter filter);
    TransactionResponse updateTransaction(Long userId, Long transactionId, TransactionRequest request);
    void deleteTransaction(Long userId, Long transactionId);
}

