package com.finance.service;

import com.finance.dto.CategoryBreakdown;
import com.finance.dto.DashboardSummary;
import com.finance.dto.MonthlyTrend;
import com.finance.model.Category;
import com.finance.model.User;
import com.finance.model.enums.TransactionType;
import com.finance.repository.TransactionRepository;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // ── Dashboard Summary ─────────────────────────────────────────────────
    // Powers the 4 cards: Income, Expenses, Savings, Savings Rate
    public DashboardSummary getSummary(String email, int month, int year) {

        User user = getUser(email);
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        BigDecimal income   = sum(user, TransactionType.INCOME,  start, end);
        BigDecimal expenses = sum(user, TransactionType.EXPENSE, start, end);
        BigDecimal savings  = income.subtract(expenses);

        // Savings rate = (savings / income) * 100
        // Guard against division by zero when no income recorded
        double savingsRate = income.compareTo(BigDecimal.ZERO) > 0
                ? savings.divide(income, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        int count = transactionRepository
                .findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(
                        user, start, end)
                .size();

        return DashboardSummary.builder()
                .totalIncome(income)
                .totalExpenses(expenses)
                .savings(savings)
                .savingsRate(round2(savingsRate))
                .transactionCount(count)
                .build();
    }

    // ── Category Breakdown ────────────────────────────────────────────────
    // Powers the pie chart — groups expenses by category with percentages
    public List<CategoryBreakdown> getCategoryBreakdown(
            String email, int month, int year) {

        User user = getUser(email);
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Object[]> rows = transactionRepository
                .getCategoryTotals(user, start, end);

        // Calculate grand total for percentage calculation
        BigDecimal total = rows.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream().map(r -> {
            Category cat    = (Category) r[0];
            BigDecimal amount = (BigDecimal) r[1];

            double pct = total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue()
                    : 0.0;

            return CategoryBreakdown.builder()
                    .categoryName(cat.getName())
                    .amount(amount)
                    .percentage(round2(pct))
                    .build();
        }).collect(Collectors.toList());
    }

    // ── Monthly Trends ────────────────────────────────────────────────────
    // Powers the bar chart — income vs expenses for last N months
    public List<MonthlyTrend> getMonthlyTrends(String email, int months) {

        User user = getUser(email);
        List<MonthlyTrend> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();

        // Build from oldest to newest — e.g. Jan, Feb, Mar, Apr, May, Jun
        for (int i = months - 1; i >= 0; i--) {
            LocalDate d     = now.minusMonths(i);
            LocalDate start = d.withDayOfMonth(1);
            LocalDate end   = d.withDayOfMonth(d.lengthOfMonth());

            trends.add(MonthlyTrend.builder()
                    .month(d.getMonth()
                            .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                            + " " + d.getYear())
                    .income(sum(user, TransactionType.INCOME,  start, end))
                    .expenses(sum(user, TransactionType.EXPENSE, start, end))
                    .build());
        }

        return trends;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private BigDecimal sum(User user, TransactionType type,
                           LocalDate start, LocalDate end) {
        return transactionRepository
                .sumByUserAndTypeAndDateBetween(user, type, start, end)
                .orElse(BigDecimal.ZERO);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }
}