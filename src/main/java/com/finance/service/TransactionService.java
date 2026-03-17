package com.finance.service;

import com.finance.dto.TransactionRequest;
import com.finance.dto.TransactionResponse;
import com.finance.model.Category;
import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.repository.CategoryRepository;
import com.finance.repository.TransactionRepository;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionResponse add(TransactionRequest req, String email) {

        User user = getUser(email);

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .amount(req.getAmount())
                .type(req.getType())
                .description(req.getDescription())
                // Default to today if frontend doesn't send a date
                .transactionDate(req.getTransactionDate() != null
                        ? req.getTransactionDate()
                        : LocalDate.now())
                .build();

        return toResponse(transactionRepository.save(transaction));
    }

    public List<TransactionResponse> getByMonth(String email, int month, int year) {

        User user = getUser(email);

        // Build date range for the given month
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return transactionRepository
                .findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(
                        user, start, end)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id, String email) {

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Security check — users can only delete their OWN transactions
        if (!transaction.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        transactionRepository.delete(transaction);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    // Converts Transaction entity → TransactionResponse DTO
    // Notice: passwordHash, user object, etc. never leave this layer
    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .categoryName(t.getCategory().getName())
                .type(t.getType())
                .description(t.getDescription())
                .transactionDate(t.getTransactionDate())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}