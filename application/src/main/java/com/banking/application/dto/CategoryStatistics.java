package com.banking.application.dto;

import com.banking.domain.model.Category;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Statistics aggregated by transaction category for a specific month.
 */
public record CategoryStatistics(
        YearMonth month,
        List<CategorySummary> categories
) {

    public static CategoryStatistics empty(YearMonth month) {
        return new CategoryStatistics(month, List.of());
    }

    public record CategorySummary(
            Category category,
            BigDecimal totalAmount,
            long transactionCount
    ) {
    }
}
