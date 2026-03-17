package com.finance.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetStatus {

    private String categoryName;
    private BigDecimal limit;
    private BigDecimal spent;
    private BigDecimal remaining;

    // e.g. 85.5 means 85.5% of budget used — drives progress bar color
    private double usagePercent;

    // true when spent > limit — triggers alert in frontend
    private boolean exceeded;
}