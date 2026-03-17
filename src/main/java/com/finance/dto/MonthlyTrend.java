package com.finance.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrend {

    // e.g. "Jan 2026", "Feb 2026" — used as x-axis labels in bar chart
    private String month;

    private BigDecimal income;
    private BigDecimal expenses;
}