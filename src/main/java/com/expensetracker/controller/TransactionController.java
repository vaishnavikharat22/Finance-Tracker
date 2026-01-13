package com.expensetracker.controller;

import com.expensetracker.dto.TransactionFilter;
import com.expensetracker.dto.TransactionRequest;
import com.expensetracker.dto.TransactionResponse;
import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Transaction Controller
 * 
 * REST API endpoints for transaction operations
 * 
 * All endpoints require authentication (JWT token)
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    /**
     * Create a new transaction
     * 
     * POST /api/v1/transactions
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        TransactionResponse response = transactionService.createTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get transaction by ID
     * 
     * GET /api/v1/transactions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        TransactionResponse response = transactionService.getTransactionById(userId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all transactions with filtering and pagination
     * 
     * GET /api/v1/transactions?page=0&size=20&type=EXPENSE&startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);

        TransactionFilter filter = TransactionFilter.builder()
                .type(type != null ? com.expensetracker.entity.Transaction.TransactionType.valueOf(type) : null)
                .categoryId(categoryId)
                .startDate(startDate)
                .endDate(endDate)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        Page<TransactionResponse> response = transactionService.getTransactions(userId, filter);
        return ResponseEntity.ok(response);
    }

    /**
     * Update transaction
     * 
     * PUT /api/v1/transactions/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        TransactionResponse response = transactionService.updateTransaction(userId, id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete transaction
     * 
     * DELETE /api/v1/transactions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extract user ID from authentication
     * 
     * The authentication object contains the UserDetails (our User entity)
     * We need to get the user ID to filter transactions
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}

