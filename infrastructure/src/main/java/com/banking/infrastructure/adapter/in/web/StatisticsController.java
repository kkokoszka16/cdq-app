package com.banking.infrastructure.adapter.in.web;

import com.banking.application.port.in.GetStatisticsUseCase;
import com.banking.infrastructure.adapter.in.web.dto.CategoryStatisticsResponse;
import com.banking.infrastructure.adapter.in.web.dto.IbanStatisticsResponse;
import com.banking.infrastructure.adapter.in.web.dto.MonthlyStatisticsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

/**
 * REST controller for statistics operations.
 */
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Transaction statistics and aggregations")
public class StatisticsController {

    private final GetStatisticsUseCase getStatisticsUseCase;

    @GetMapping("/by-category")
    @Cacheable(value = "categoryStats", key = "#month")
    @Operation(summary = "Get statistics aggregated by category for a month")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved")
    })
    public CategoryStatisticsResponse getStatisticsByCategory(
            @Parameter(description = "Month (YYYY-MM)", required = true, example = "2024-01")
            @RequestParam("month") String month
    ) {
        var yearMonth = parseYearMonth(month);
        var statistics = getStatisticsUseCase.getStatisticsByCategory(yearMonth);

        return CategoryStatisticsResponse.from(statistics);
    }

    @GetMapping("/by-iban")
    @Cacheable(value = "ibanStats", key = "#month")
    @Operation(summary = "Get statistics aggregated by IBAN for a month")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved")
    })
    public IbanStatisticsResponse getStatisticsByIban(
            @Parameter(description = "Month (YYYY-MM)", required = true, example = "2024-01")
            @RequestParam("month") String month
    ) {
        var yearMonth = parseYearMonth(month);
        var statistics = getStatisticsUseCase.getStatisticsByIban(yearMonth);

        return IbanStatisticsResponse.from(statistics);
    }

    @GetMapping("/by-month")
    @Cacheable(value = "monthlyStats", key = "#year")
    @Operation(summary = "Get statistics aggregated by month for a year")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved")
    })
    public MonthlyStatisticsResponse getStatisticsByMonth(
            @Parameter(description = "Year", required = true, example = "2024")
            @RequestParam("year") int year
    ) {
        var statistics = getStatisticsUseCase.getStatisticsByMonth(year);

        return MonthlyStatisticsResponse.from(statistics);
    }

    private YearMonth parseYearMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid month format. Expected: YYYY-MM");
        }
    }
}
