package com.finance.controller;

import com.finance.dto.CategoryBreakdown;
import com.finance.dto.MonthlyTrend;
import com.finance.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // GET /api/analytics/categories?month=3&year=2026
    // Returns expense breakdown by category — powers the pie chart
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryBreakdown>> categories(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year,
            @AuthenticationPrincipal UserDetails userDetails) {

        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(analyticsService.getCategoryBreakdown(
                userDetails.getUsername(),
                month == 0 ? now.getMonthValue() : month,
                year  == 0 ? now.getYear()       : year));
    }

    // GET /api/analytics/monthly?months=6
    // Returns last N months of income vs expenses — powers the bar chart
    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyTrend>> monthly(
            @RequestParam(defaultValue = "6") int months,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(analyticsService.getMonthlyTrends(
                userDetails.getUsername(), months));
    }
}