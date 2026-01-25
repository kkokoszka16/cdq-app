package com.banking.application.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Statistics aggregated by IBAN for a specific month.
 */
public record IbanStatistics(
        YearMonth month,
        List<IbanSummary> ibans
) {

    public static IbanStatistics empty(YearMonth month) {
        return new IbanStatistics(month, List.of());
    }

    public record IbanSummary(
            String iban,
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal balance
    ) {

        public IbanSummary(String iban, BigDecimal totalIncome, BigDecimal totalExpense) {
            this(iban, totalIncome, totalExpense, totalIncome.add(totalExpense));
        }
    }
}
