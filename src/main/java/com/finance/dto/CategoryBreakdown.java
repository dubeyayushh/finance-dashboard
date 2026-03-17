package com.finance.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBreakdown {

    private String categoryName;
    private BigDecimal amount;

    // Percentage of total expenses — used for pie chart
    private double percentage;
}