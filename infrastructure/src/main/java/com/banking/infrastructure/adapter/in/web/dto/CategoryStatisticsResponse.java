package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.CategoryStatistics;
import com.banking.domain.model.Category;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Response DTO for category statistics.
 */
@Schema(description = "Statistics aggregated by category")
public record CategoryStatisticsResponse(

        @Schema(description = "Month for statistics", example = "2024-01")
        String month,

        @Schema(description = "Category summaries")
        List<CategorySummaryDto> categories
) {

    public static CategoryStatisticsResponse from(CategoryStatistics statistics) {
        var summaries = statistics.categories().stream()
                .map(summary -> new CategorySummaryDto(
                        summary.category(),
                        summary.totalAmount(),
                        summary.transactionCount()
                ))
                .toList();

        return new CategoryStatisticsResponse(statistics.month().toString(), summaries);
    }

    @Schema(description = "Summary for a single category")
    public record CategorySummaryDto(

            @Schema(description = "Transaction category")
            Category category,

            @Schema(description = "Total amount for category", example = "1500.00")
            BigDecimal totalAmount,

            @Schema(description = "Number of transactions", example = "25")
            long transactionCount
    ) {
    }
}
