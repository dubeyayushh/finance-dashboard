package com.finance.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetStatus {

    private Long id;           // needed by frontend for edit/delete
    private String categoryName;
    private BigDecimal limit;
    private BigDecimal spent;
    private BigDecimal remaining;
    private double usagePercent;
    private boolean exceeded;
}