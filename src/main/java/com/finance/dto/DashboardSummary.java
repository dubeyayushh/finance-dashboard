package com.finance.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal savings;

    // Calculated as (savings / income) * 100
    private double savingsRate;

    private int transactionCount;
}