package com.banking.application.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Statistics aggregated by month for a specific year.
 */
public record MonthlyStatistics(
        int year,
        List<MonthlySummary> months
) {

    public static MonthlyStatistics empty(int year) {
        return new MonthlyStatistics(year, List.of());
    }

    public record MonthlySummary(
            YearMonth month,
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal balance
    ) {

        public MonthlySummary(YearMonth month, BigDecimal totalIncome, BigDecimal totalExpense) {
            this(month, totalIncome, totalExpense, totalIncome.add(totalExpense));
        }
    }
}
