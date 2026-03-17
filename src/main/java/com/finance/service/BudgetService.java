package com.finance.service;

import com.finance.dto.BudgetRequest;
import com.finance.dto.BudgetStatus;
import com.finance.model.Budget;
import com.finance.model.Category;
import com.finance.model.User;
import com.finance.repository.BudgetRepository;
import com.finance.repository.CategoryRepository;
import com.finance.repository.TransactionRepository;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    // ── Set Budget ────────────────────────────────────────────────────────
    // Creates a new budget OR updates existing one for same user + category
    public Budget set(BudgetRequest req, String email) {

        User user = getUser(email);
        Category category = categoryRepository
                .findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Upsert pattern — update if exists, create if not
        Budget budget = budgetRepository
                .findByUserAndCategory(user, category)
                .orElse(Budget.builder()
                        .user(user)
                        .category(category)
                        .build());

        budget.setMonthlyLimit(req.getMonthlyLimit());
        return budgetRepository.save(budget);
    }

    // ── Budget Status ─────────────────────────────────────────────────────
    // For each budget: calculates spent, remaining, % used, exceeded flag
    // Powers the progress bars in the Budgets tab
    public List<BudgetStatus> getStatus(String email, int month, int year) {

        User user = getUser(email);
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());

        return budgetRepository.findByUser(user).stream().map(b -> {

            // How much was actually spent in this category this month
            BigDecimal spent = transactionRepository
                    .sumByCategoryAndDateBetween(
                            user, b.getCategory(), start, end)
                    .orElse(BigDecimal.ZERO);

            BigDecimal remaining = b.getMonthlyLimit().subtract(spent);

            // Usage percentage — capped display at 100% in frontend
            double pct = b.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0
                    ? spent.divide(b.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue()
                    : 0.0;

            return BudgetStatus.builder()
                    .categoryName(b.getCategory().getName())
                    .limit(b.getMonthlyLimit())
                    .spent(spent)
                    .remaining(remaining)
                    .usagePercent(Math.round(pct * 100.0) / 100.0)
                    // exceeded flag drives the red alert in the frontend
                    .exceeded(spent.compareTo(b.getMonthlyLimit()) > 0)
                    .build();

        }).collect(Collectors.toList());
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }
}
