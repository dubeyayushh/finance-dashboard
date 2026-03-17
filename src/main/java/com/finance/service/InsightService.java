package com.finance.service;

import com.finance.dto.InsightResponse;
import com.finance.model.Budget;
import com.finance.model.Category;
import com.finance.model.Insight;
import com.finance.model.User;
import com.finance.model.enums.InsightSeverity;
import com.finance.model.enums.TransactionType;
import com.finance.repository.BudgetRepository;
import com.finance.repository.InsightRepository;
import com.finance.repository.TransactionRepository;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightService {

    private final InsightRepository insightRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    // ── Generate & Save ───────────────────────────────────────────────────
    // @Transactional ensures delete + save happen atomically
    // If anything fails midway, the whole operation rolls back
    @Transactional
    public List<InsightResponse> generateAndSave(String email) {

        User user = userRepository.findByEmail(email).orElseThrow();

        // Step 1: Wipe old insights before generating fresh ones
        insightRepository.deleteByUser(user);

        List<Insight> insights = new ArrayList<>();
        LocalDate now = LocalDate.now();

        // ── Date ranges ───────────────────────────────────────────────────
        LocalDate curStart  = now.withDayOfMonth(1);
        LocalDate curEnd    = now.withDayOfMonth(now.lengthOfMonth());
        LocalDate prevMonth = now.minusMonths(1);
        LocalDate prevStart = prevMonth.withDayOfMonth(1);
        LocalDate prevEnd   = prevMonth.withDayOfMonth(prevMonth.lengthOfMonth());

        BigDecimal curIncome    = sum(user, TransactionType.INCOME,  curStart,  curEnd);
        BigDecimal curExpenses  = sum(user, TransactionType.EXPENSE, curStart,  curEnd);
        BigDecimal prevExpenses = sum(user, TransactionType.EXPENSE, prevStart, prevEnd);

        // ── Rule 1: Month-over-month spending change ──────────────────────
        // Compares this month's total expenses vs last month's
        if (prevExpenses.compareTo(BigDecimal.ZERO) > 0) {
            double changePct = curExpenses
                    .subtract(prevExpenses)
                    .divide(prevExpenses, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            if (changePct > 15) {
                add(insights, user,
                        String.format(
                                "⚠️ Total spending rose %.1f%% vs last month (₹%.0f → ₹%.0f).",
                                changePct, prevExpenses, curExpenses),
                        InsightSeverity.WARNING);

            } else if (changePct < -10) {
                add(insights, user,
                        String.format(
                                "✅ Great discipline! Spending dropped %.1f%% vs last month.",
                                Math.abs(changePct)),
                        InsightSeverity.INFO);
            }
        }

        // ── Rule 2: Savings rate analysis ────────────────────────────────
        // Savings rate = (income - expenses) / income * 100
        // Below 10% is critical, above 30% is excellent
        if (curIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savings = curIncome.subtract(curExpenses);
            double rate = savings
                    .divide(curIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            if (rate < 10) {
                add(insights, user,
                        String.format(
                                "🔴 Savings rate is only %.1f%% this month. " +
                                        "Target >= 20%% for financial safety.", rate),
                        InsightSeverity.ALERT);

            } else if (rate >= 30) {
                add(insights, user,
                        String.format(
                                "💰 Excellent! Savings rate is %.1f%% — " +
                                        "well above the 20%% benchmark.", rate),
                        InsightSeverity.INFO);
            }
        }

        // ── Rule 3: Budget violations & near-breach warnings ─────────────
        // Checks every budget the user has set
        List<Budget> budgets = budgetRepository.findByUser(user);
        for (Budget b : budgets) {
            BigDecimal spent = transactionRepository
                    .sumByCategoryAndDateBetween(
                            user, b.getCategory(), curStart, curEnd)
                    .orElse(BigDecimal.ZERO);

            if (spent.compareTo(b.getMonthlyLimit()) > 0) {
                // Budget already exceeded
                BigDecimal over = spent.subtract(b.getMonthlyLimit());
                add(insights, user,
                        String.format(
                                "🚨 Budget exceeded for %s: spent ₹%.0f / " +
                                        "limit ₹%.0f (over by ₹%.0f).",
                                b.getCategory().getName(),
                                spent, b.getMonthlyLimit(), over),
                        InsightSeverity.ALERT);

            } else if (b.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0) {
                // Check if approaching the limit (> 80% used)
                double used = spent
                        .divide(b.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();

                if (used > 80) {
                    add(insights, user,
                            String.format(
                                    "⚠️ %s budget %.0f%% used — ₹%.0f remaining this month.",
                                    b.getCategory().getName(),
                                    used,
                                    b.getMonthlyLimit().subtract(spent)),
                            InsightSeverity.WARNING);
                }
            }
        }

        // ── Rule 4: 3-month category anomaly detection ───────────────────
        // If current month spending in a category is 50% above 3-month average
        // that's an anomaly worth flagging
        List<Object[]> curCats = transactionRepository
                .getCategoryTotals(user, curStart, curEnd);

        for (Object[] row : curCats) {
            Category cat      = (Category) row[0];
            BigDecimal curAmt = (BigDecimal) row[1];

            BigDecimal histTotal = BigDecimal.ZERO;
            int validMonths = 0;

            // Look back 3 months and average the spend for this category
            for (int i = 1; i <= 3; i++) {
                LocalDate past      = now.minusMonths(i);
                LocalDate pastStart = past.withDayOfMonth(1);
                LocalDate pastEnd   = past.withDayOfMonth(past.lengthOfMonth());

                BigDecimal pastAmt = transactionRepository
                        .sumByCategoryAndDateBetween(
                                user, cat, pastStart, pastEnd)
                        .orElse(BigDecimal.ZERO);

                if (pastAmt.compareTo(BigDecimal.ZERO) > 0) {
                    histTotal = histTotal.add(pastAmt);
                    validMonths++;
                }
            }

            // Only flag if we have at least 2 months of history to compare
            if (validMonths >= 2) {
                BigDecimal avg = histTotal.divide(
                        BigDecimal.valueOf(validMonths), 2, RoundingMode.HALF_UP);

                if (avg.compareTo(BigDecimal.ZERO) > 0) {
                    double ratio = curAmt
                            .divide(avg, 4, RoundingMode.HALF_UP)
                            .doubleValue();

                    if (ratio > 1.5) {
                        add(insights, user,
                                String.format(
                                        "📊 %s is %.0f%% above your 3-month average " +
                                                "(₹%.0f vs avg ₹%.0f).",
                                        cat.getName(),
                                        (ratio - 1) * 100,
                                        curAmt, avg),
                                InsightSeverity.WARNING);
                    }
                }
            }
        }

        // ── Rule 5: Smart reduction tip ───────────────────────────────────
        // Finds the biggest expense category and suggests a 10% cut
        curCats.stream()
                .max(Comparator.comparing(r -> (BigDecimal) r[1]))
                .ifPresent(r -> {
                    Category cat  = (Category) r[0];
                    BigDecimal amt = (BigDecimal) r[1];
                    BigDecimal save10 = amt
                            .multiply(BigDecimal.valueOf(0.10))
                            .setScale(0, RoundingMode.HALF_UP);

                    add(insights, user,
                            String.format(
                                    "💡 Cutting %s spending by 10%% could " +
                                            "save you ₹%.0f this month.",
                                    cat.getName(), save10),
                            InsightSeverity.INFO);
                });

        // ── Rule 6: No income warning ─────────────────────────────────────
        // Reminds user to log their salary if no income is recorded
        if (curIncome.compareTo(BigDecimal.ZERO) == 0) {
            add(insights, user,
                    "ℹ️ No income recorded this month. " +
                            "Add your salary or income source for accurate insights.",
                    InsightSeverity.INFO);
        }

        // Save all insights and return as DTOs
        return insightRepository.saveAll(insights)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Get Saved Insights ────────────────────────────────────────────────
    // Returns previously generated insights without re-running analysis
    public List<InsightResponse> getInsights(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return insightRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void add(List<Insight> list, User user,
                     String message, InsightSeverity severity) {
        list.add(Insight.builder()
                .user(user)
                .message(message)
                .severity(severity)
                .build());
    }

    private BigDecimal sum(User user, TransactionType type,
                           LocalDate start, LocalDate end) {
        return transactionRepository
                .sumByUserAndTypeAndDateBetween(user, type, start, end)
                .orElse(BigDecimal.ZERO);
    }

    private InsightResponse toDto(Insight i) {
        return InsightResponse.builder()
                .id(i.getId())
                .message(i.getMessage())
                .severity(i.getSeverity())
                .createdAt(i.getCreatedAt())
                .build();
    }
}