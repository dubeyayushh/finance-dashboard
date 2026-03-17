package com.finance.dto;

import com.finance.model.enums.TransactionType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;

    // We send category name as string — not the whole Category object
    // This keeps the response clean and avoids circular references
    private String categoryName;

    private TransactionType type;
    private String description;
    private LocalDate transactionDate;
}