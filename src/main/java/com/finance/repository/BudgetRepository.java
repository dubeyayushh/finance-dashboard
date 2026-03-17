package com.finance.repository;

import com.finance.model.Budget;
import com.finance.model.Category;
import com.finance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Get all budgets for a user — used to display budget status list
    List<Budget> findByUser(User user);

    // Find a specific budget by user + category
    // Used during set budget — if exists update it, else create new
    Optional<Budget> findByUserAndCategory(User user, Category category);
}