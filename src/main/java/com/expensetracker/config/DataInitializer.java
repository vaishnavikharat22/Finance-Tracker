package com.expensetracker.config;

import com.expensetracker.entity.Category;
import com.expensetracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Data Initializer
 * 
 * This class runs when the application starts.
 * It seeds the database with default categories.
 * 
 * Why do we need this?
 * - Users need categories to create transactions
 * - Default categories provide a starting point
 * - Better UX - users don't start with empty categories
 * 
 * @Component - Spring will create an instance of this class
 * CommandLineRunner - Runs after application context is loaded
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        // Check if default categories already exist
        if (categoryRepository.findByIsDefaultTrue().isEmpty()) {
            log.info("Initializing default categories...");
            createDefaultCategories();
            log.info("Default categories created successfully!");
        } else {
            log.info("Default categories already exist, skipping initialization.");
        }
    }

    /**
     * Create default categories
     * 
     * These categories are available to all users
     */
    private void createDefaultCategories() {
        List<Category> defaultCategories = Arrays.asList(
            // Expense Categories
            createCategory("Food & Dining", Category.TransactionType.EXPENSE, "food", "#FF6B6B"),
            createCategory("Transportation", Category.TransactionType.EXPENSE, "car", "#4ECDC4"),
            createCategory("Shopping", Category.TransactionType.EXPENSE, "shopping", "#95E1D3"),
            createCategory("Bills & Utilities", Category.TransactionType.EXPENSE, "bills", "#F38181"),
            createCategory("Entertainment", Category.TransactionType.EXPENSE, "entertainment", "#AA96DA"),
            createCategory("Healthcare", Category.TransactionType.EXPENSE, "healthcare", "#FCBAD3"),
            createCategory("Education", Category.TransactionType.EXPENSE, "education", "#A8E6CF"),
            createCategory("Travel", Category.TransactionType.EXPENSE, "travel", "#FFD3A5"),
            createCategory("Personal Care", Category.TransactionType.EXPENSE, "personal", "#C7CEEA"),
            createCategory("Other Expenses", Category.TransactionType.EXPENSE, "other", "#B4B4B4"),

            // Income Categories
            createCategory("Salary", Category.TransactionType.INCOME, "salary", "#51CF66"),
            createCategory("Freelance", Category.TransactionType.INCOME, "freelance", "#74C0FC"),
            createCategory("Investment", Category.TransactionType.INCOME, "investment", "#FFD43B"),
            createCategory("Business", Category.TransactionType.INCOME, "business", "#FF8787"),
            createCategory("Other Income", Category.TransactionType.INCOME, "other", "#B4B4B4")
        );

        categoryRepository.saveAll(defaultCategories);
    }

    /**
     * Helper method to create a category
     */
    private Category createCategory(String name, Category.TransactionType type, String icon, String color) {
        return Category.builder()
                .name(name)
                .type(type)
                .icon(icon)
                .color(color)
                .isDefault(true)
                .user(null) // Default categories don't belong to any user
                .build();
    }
}

