package com.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application
 * 
 * This is the entry point of the Expense Tracker API.
 * Spring Boot will automatically:
 * - Scan for components in this package and sub-packages
 * - Configure Spring Security
 * - Set up JPA repositories
 * - Initialize the application context
 */
@SpringBootApplication
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }
}

