package com.finance.dto;

import com.finance.model.enums.TransactionType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {

    private BigDecimal amount;

    // Frontend sends category ID, not the whole category object
    private Long categoryId;

    private TransactionType type;

    private String description;

    // Optional — defaults to today if not provided
    private LocalDate transactionDate;
}