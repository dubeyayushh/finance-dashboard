package com.finance.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetRequest {
    private Long categoryId;
    private BigDecimal monthlyLimit;
}