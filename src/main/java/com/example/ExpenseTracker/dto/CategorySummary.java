package com.example.ExpenseTracker.dto;

import com.example.ExpenseTracker.entity.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategorySummary {
    private ExpenseCategory category;
    private BigDecimal totalAmount;
    private Double percentage;
}
