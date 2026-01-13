package com.expensetracker.service.impl;

import com.expensetracker.dto.TransactionFilter;
import com.expensetracker.dto.TransactionRequest;
import com.expensetracker.dto.TransactionResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Transaction Service Implementation
 * 
 * Business logic for transaction operations
 * 
 * Key responsibilities:
 * - Validate user owns the transaction
 * - Validate category belongs to user
 * - Handle pagination and filtering
 * - Map entities to DTOs
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Create a new transaction
     * 
     * Steps:
     * 1. Validate user exists
     * 2. Validate category exists and belongs to user (or is default)
     * 3. Create transaction entity
     * 4. Save to database
     * 5. Return response DTO
     */
    @Override
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        // Step 1: Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: Load and validate category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Step 3: Validate category belongs to user or is default
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new RuntimeException("Category does not belong to user");
        }

        // Step 4: Validate category type matches transaction type
        if (!category.getType().equals(request.getType())) {
            throw new RuntimeException("Category type does not match transaction type");
        }

        // Step 5: Create transaction entity
        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .type(request.getType())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate() != null 
                    ? request.getTransactionDate() 
                    : LocalDate.now())
                .build();

        // Step 6: Save to database
        transaction = transactionRepository.save(transaction);

        // Step 7: Map to response DTO
        return mapToResponse(transaction);
    }

    /**
     * Get transaction by ID
     * 
     * Security: Only returns transaction if it belongs to the user
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Security check: Ensure transaction belongs to user
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Transaction does not belong to user");
        }

        return mapToResponse(transaction);
    }

    /**
     * Get transactions with filtering and pagination
     * 
     * Supports:
     * - Filtering by type, category, date range
     * - Pagination (page, size)
     * - Sorting (by date, amount, etc.)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(Long userId, TransactionFilter filter) {
        // Step 1: Build pagination and sorting
        Sort sort = Sort.by(
            filter.getSortDir().equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC,
            filter.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // Step 2: Apply filters and fetch transactions
        Page<Transaction> transactions;

        if (filter.getStartDate() != null && filter.getEndDate() != null) {
            // Filter by date range
            if (filter.getType() != null) {
                // Filter by date range and type
                transactions = transactionRepository.findByUserIdAndTypeOrderByTransactionDateDesc(
                    userId, filter.getType(), pageable
                );
            } else {
                // Filter by date range only
                transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                    userId, filter.getStartDate(), filter.getEndDate(), pageable
                );
            }
        } else if (filter.getType() != null) {
            // Filter by type only
            transactions = transactionRepository.findByUserIdAndTypeOrderByTransactionDateDesc(
                userId, filter.getType(), pageable
            );
        } else {
            // No filters, get all transactions
            transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
        }

        // Step 3: Map to response DTOs
        return transactions.map(this::mapToResponse);
    }

    /**
     * Update transaction
     * 
     * Security: Only allows update if transaction belongs to user
     */
    @Override
    public TransactionResponse updateTransaction(Long userId, Long transactionId, TransactionRequest request) {
        // Step 1: Load transaction
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Step 2: Security check
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Transaction does not belong to user");
        }

        // Step 3: Load and validate category if changed
        if (!transaction.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
                throw new RuntimeException("Category does not belong to user");
            }

            if (!category.getType().equals(request.getType())) {
                throw new RuntimeException("Category type does not match transaction type");
            }

            transaction.setCategory(category);
        }

        // Step 4: Update fields
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }

        // Step 5: Save updated transaction
        transaction = transactionRepository.save(transaction);

        // Step 6: Return response
        return mapToResponse(transaction);
    }

    /**
     * Delete transaction
     * 
     * Security: Only allows delete if transaction belongs to user
     */
    @Override
    public void deleteTransaction(Long userId, Long transactionId) {
        // Step 1: Load transaction
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Step 2: Security check
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Transaction does not belong to user");
        }

        // Step 3: Delete
        transactionRepository.delete(transaction);
    }

    /**
     * Map Transaction entity to TransactionResponse DTO
     * 
     * Why manual mapping instead of MapStruct?
     * - For beginners, manual mapping is clearer
     * - Can add custom logic easily
     * - MapStruct can be added later for optimization
     */
    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .category(TransactionResponse.CategoryInfo.builder()
                        .id(transaction.getCategory().getId())
                        .name(transaction.getCategory().getName())
                        .icon(transaction.getCategory().getIcon())
                        .color(transaction.getCategory().getColor())
                        .build())
                .build();
    }
}

