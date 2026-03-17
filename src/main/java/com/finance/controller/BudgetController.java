package com.finance.controller;

import com.finance.dto.BudgetRequest;
import com.finance.dto.BudgetStatus;
import com.finance.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    // POST /api/budgets
    // Creates or updates a monthly budget for a category
    @PostMapping
    public ResponseEntity<?> set(
            @RequestBody BudgetRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                budgetService.set(req, userDetails.getUsername()));
    }

    // GET /api/budgets/status?month=3&year=2026
    // Returns budget vs actual spend for each category this month
    @GetMapping("/status")
    public ResponseEntity<List<BudgetStatus>> status(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year,
            @AuthenticationPrincipal UserDetails userDetails) {

        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(budgetService.getStatus(
                userDetails.getUsername(),
                month == 0 ? now.getMonthValue() : month,
                year  == 0 ? now.getYear()       : year));
    }

    // DELETE /api/budgets/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        budgetService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}