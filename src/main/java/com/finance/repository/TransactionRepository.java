package com.finance.repository;

import com.finance.model.Category;
import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Fetch all transactions for a user within a date range — used for monthly view
    List<Transaction> findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(
            User user, LocalDate start, LocalDate end);

    // Fetch all transactions for a user — used for report generation
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);

    // Sum total INCOME or EXPENSE for a user in a date range — used for dashboard summary
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user = :user AND t.type = :type " +
            "AND t.transactionDate BETWEEN :start AND :end")
    Optional<BigDecimal> sumByUserAndTypeAndDateBetween(
            @Param("user") User user,
            @Param("type") TransactionType type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    // Sum total EXPENSE for a specific category — used for budget status check
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user = :user AND t.category = :category " +
            "AND t.type = com.finance.model.enums.TransactionType.EXPENSE " +
            "AND t.transactionDate BETWEEN :start AND :end")
    Optional<BigDecimal> sumByCategoryAndDateBetween(
            @Param("user") User user,
            @Param("category") Category category,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    // Group expenses by category with totals — used for pie chart breakdown
    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t " +
            "WHERE t.user = :user " +
            "AND t.type = com.finance.model.enums.TransactionType.EXPENSE " +
            "AND t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> getCategoryTotals(
            @Param("user") User user,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}