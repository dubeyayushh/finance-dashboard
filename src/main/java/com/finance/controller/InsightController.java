package com.finance.controller;

import com.finance.dto.InsightResponse;
import com.finance.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    // GET /api/insights
    // Returns previously generated insights without re-running analysis
    @GetMapping
    public ResponseEntity<List<InsightResponse>> get(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                insightService.getInsights(userDetails.getUsername()));
    }

    // POST /api/insights/refresh
    // Wipes old insights, runs all 6 analysis rules, returns fresh insights
    @PostMapping("/refresh")
    public ResponseEntity<List<InsightResponse>> refresh(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                insightService.generateAndSave(userDetails.getUsername()));
    }
}