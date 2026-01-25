package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.IbanStatistics;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for IBAN statistics.
 */
@Schema(description = "Statistics aggregated by IBAN")
public record IbanStatisticsResponse(

        @Schema(description = "Month for statistics", example = "2024-01")
        String month,

        @Schema(description = "IBAN summaries")
        List<IbanSummaryDto> ibans
) {

    public static IbanStatisticsResponse from(IbanStatistics statistics) {
        var summaries = statistics.ibans().stream()
                .map(summary -> new IbanSummaryDto(
                        summary.iban(),
                        summary.totalIncome(),
                        summary.totalExpense(),
                        summary.balance()
                ))
                .toList();

        return new IbanStatisticsResponse(statistics.month().toString(), summaries);
    }

    @Schema(description = "Summary for a single IBAN")
    public record IbanSummaryDto(

            @Schema(description = "International Bank Account Number", example = "PL61109010140000071219812874")
            String iban,

            @Schema(description = "Total income", example = "5000.00")
            BigDecimal totalIncome,

            @Schema(description = "Total expenses (negative)", example = "-2000.00")
            BigDecimal totalExpense,

            @Schema(description = "Balance (income + expense)", example = "3000.00")
            BigDecimal balance
    ) {
    }
}
