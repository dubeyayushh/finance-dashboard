package com.finance.config;

import com.finance.model.Category;
import com.finance.model.enums.TransactionType;
import com.finance.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    // ApplicationRunner runs once automatically after Spring Boot starts
    @Override
    public void run(ApplicationArguments args) {

        // Guard — only seed if table is empty
        // This prevents duplicate inserts on every restart
        if (categoryRepository.count() > 0) return;

        categoryRepository.saveAll(List.of(

                // ── Income categories ────────────────────────────────────
                cat("Salary",        TransactionType.INCOME),
                cat("Freelance",     TransactionType.INCOME),
                cat("Investment",    TransactionType.INCOME),
                cat("Other Income",  TransactionType.INCOME),

                // ── Expense categories ───────────────────────────────────
                cat("Food",          TransactionType.EXPENSE),
                cat("Transport",     TransactionType.EXPENSE),
                cat("Entertainment", TransactionType.EXPENSE),
                cat("Shopping",      TransactionType.EXPENSE),
                cat("Utilities",     TransactionType.EXPENSE),
                cat("Rent",          TransactionType.EXPENSE),
                cat("Health",        TransactionType.EXPENSE),
                cat("Education",     TransactionType.EXPENSE)
        ));

        System.out.println("✅ Categories seeded successfully.");
    }

    private Category cat(String name, TransactionType type) {
        return Category.builder().name(name).type(type).build();
    }
}