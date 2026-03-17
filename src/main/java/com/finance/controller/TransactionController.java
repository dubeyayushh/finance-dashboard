package com.finance.controller;

import com.finance.dto.TransactionRequest;
import com.finance.dto.TransactionResponse;
import com.finance.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // POST /api/transactions
    @PostMapping
    public ResponseEntity<TransactionResponse> add(
            @RequestBody TransactionRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                transactionService.add(req, userDetails.getUsername()));
    }

    // GET /api/transactions?month=3&year=2026
    // @AuthenticationPrincipal extracts the logged-in user from the JWT token
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> list(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year,
            @AuthenticationPrincipal UserDetails userDetails) {

        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(transactionService.getByMonth(
                userDetails.getUsername(),
                month == 0 ? now.getMonthValue() : month,
                year  == 0 ? now.getYear()       : year));
    }

    // DELETE /api/transactions/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        transactionService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}