package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.MonthlyStatistics;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for monthly statistics.
 */
@Schema(description = "Statistics aggregated by month")
public record MonthlyStatisticsResponse(

        @Schema(description = "Year for statistics", example = "2024")
        int year,

        @Schema(description = "Monthly summaries")
        List<MonthlySummaryDto> months
) {

    public static MonthlyStatisticsResponse from(MonthlyStatistics statistics) {
        var summaries = statistics.months().stream()
                .map(summary -> new MonthlySummaryDto(
                        summary.month().toString(),
                        summary.totalIncome(),
                        summary.totalExpense(),
                        summary.balance()
                ))
                .toList();

        return new MonthlyStatisticsResponse(statistics.year(), summaries);
    }

    @Schema(description = "Summary for a single month")
    public record MonthlySummaryDto(

            @Schema(description = "Month", example = "2024-01")
            String month,

            @Schema(description = "Total income", example = "5000.00")
            BigDecimal totalIncome,

            @Schema(description = "Total expenses (negative)", example = "-2000.00")
            BigDecimal totalExpense,

            @Schema(description = "Balance (income + expense)", example = "3000.00")
            BigDecimal balance
    ) {
    }
}
