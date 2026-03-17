package com.finance.controller;

import com.finance.dto.DashboardSummary;
import com.finance.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AnalyticsService analyticsService;

    // GET /api/dashboard/summary?month=3&year=2026
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummary> summary(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year,
            @AuthenticationPrincipal UserDetails userDetails) {

        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(analyticsService.getSummary(
                userDetails.getUsername(),
                month == 0 ? now.getMonthValue() : month,
                year  == 0 ? now.getYear()       : year));
    }
}