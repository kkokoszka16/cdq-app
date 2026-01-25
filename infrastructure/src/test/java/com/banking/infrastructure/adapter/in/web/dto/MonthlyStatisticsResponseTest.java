package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.MonthlyStatistics;
import com.banking.application.dto.MonthlyStatistics.MonthlySummary;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MonthlyStatisticsResponse")
class MonthlyStatisticsResponseTest {

    private static final int TEST_YEAR = 2024;

    @Nested
    @DisplayName("given MonthlyStatistics with data")
    class GivenMonthlyStatisticsWithData {

        @Test
        @DisplayName("when from called then maps all fields correctly")
        void given_statistics_when_from_then_maps_correctly() {
            // given
            var summaries = List.of(
                    new MonthlySummary(
                            YearMonth.of(2024, 1),
                            new BigDecimal("5000.00"),
                            new BigDecimal("-3000.00"),
                            new BigDecimal("2000.00")
                    ),
                    new MonthlySummary(
                            YearMonth.of(2024, 2),
                            new BigDecimal("4500.00"),
                            new BigDecimal("-2800.00"),
                            new BigDecimal("1700.00")
                    ),
                    new MonthlySummary(
                            YearMonth.of(2024, 3),
                            new BigDecimal("5200.00"),
                            new BigDecimal("-3100.00"),
                            new BigDecimal("2100.00")
                    )
            );
            var statistics = new MonthlyStatistics(TEST_YEAR, summaries);

            // when
            var response = MonthlyStatisticsResponse.from(statistics);

            // then
            assertThat(response.year()).isEqualTo(TEST_YEAR);
            assertThat(response.months()).hasSize(3);
        }

        @Test
        @DisplayName("when from called then maps monthly summaries correctly")
        void given_statistics_when_from_then_maps_summaries() {
            // given
            var summaries = List.of(
                    new MonthlySummary(
                            YearMonth.of(2024, 6),
                            new BigDecimal("8000.00"),
                            new BigDecimal("-4500.00"),
                            new BigDecimal("3500.00")
                    )
            );
            var statistics = new MonthlyStatistics(TEST_YEAR, summaries);

            // when
            var response = MonthlyStatisticsResponse.from(statistics);

            // then
            var monthDto = response.months().get(0);
            assertThat(monthDto.month()).isEqualTo("2024-06");
            assertThat(monthDto.totalIncome()).isEqualByComparingTo("8000.00");
            assertThat(monthDto.totalExpense()).isEqualByComparingTo("-4500.00");
            assertThat(monthDto.balance()).isEqualByComparingTo("3500.00");
        }
    }

    @Nested
    @DisplayName("given empty MonthlyStatistics")
    class GivenEmptyMonthlyStatistics {

        @Test
        @DisplayName("when from called then returns response with empty months")
        void given_empty_statistics_when_from_then_empty_months() {
            // given
            var statistics = MonthlyStatistics.empty(TEST_YEAR);

            // when
            var response = MonthlyStatisticsResponse.from(statistics);

            // then
            assertThat(response.year()).isEqualTo(TEST_YEAR);
            assertThat(response.months()).isEmpty();
        }
    }

    @Nested
    @DisplayName("given MonthlySummaryDto record")
    class GivenMonthlySummaryDto {

        @Test
        @DisplayName("when created then all fields accessible")
        void given_dto_when_created_then_fields_accessible() {
            // given
            var month = "2024-09";
            var totalIncome = new BigDecimal("12000.00");
            var totalExpense = new BigDecimal("-7500.00");
            var balance = new BigDecimal("4500.00");

            // when
            var dto = new MonthlyStatisticsResponse.MonthlySummaryDto(
                    month,
                    totalIncome,
                    totalExpense,
                    balance
            );

            // then
            assertThat(dto.month()).isEqualTo(month);
            assertThat(dto.totalIncome()).isEqualByComparingTo("12000.00");
            assertThat(dto.totalExpense()).isEqualByComparingTo("-7500.00");
            assertThat(dto.balance()).isEqualByComparingTo("4500.00");
        }

        @Test
        @DisplayName("when two dtos have same values then equals returns true")
        void given_same_values_when_equals_then_true() {
            // given
            var dto1 = new MonthlyStatisticsResponse.MonthlySummaryDto(
                    "2024-04",
                    new BigDecimal("3000.00"),
                    new BigDecimal("-1500.00"),
                    new BigDecimal("1500.00")
            );
            var dto2 = new MonthlyStatisticsResponse.MonthlySummaryDto(
                    "2024-04",
                    new BigDecimal("3000.00"),
                    new BigDecimal("-1500.00"),
                    new BigDecimal("1500.00")
            );

            // then
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }

        @Test
        @DisplayName("when balance is negative then correctly represented")
        void given_negative_balance_when_created_then_correct() {
            // given
            var dto = new MonthlyStatisticsResponse.MonthlySummaryDto(
                    "2024-11",
                    new BigDecimal("2000.00"),
                    new BigDecimal("-3000.00"),
                    new BigDecimal("-1000.00")
            );

            // then
            assertThat(dto.balance()).isNegative();
            assertThat(dto.balance()).isEqualByComparingTo("-1000.00");
        }

        @Test
        @DisplayName("when balance is zero then correctly represented")
        void given_zero_balance_when_created_then_correct() {
            // given
            var dto = new MonthlyStatisticsResponse.MonthlySummaryDto(
                    "2024-08",
                    new BigDecimal("5000.00"),
                    new BigDecimal("-5000.00"),
                    BigDecimal.ZERO
            );

            // then
            assertThat(dto.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("given MonthlyStatisticsResponse record")
    class GivenMonthlyStatisticsResponseRecord {

        @Test
        @DisplayName("when created then all fields accessible")
        void given_response_when_created_then_fields_accessible() {
            // given
            var months = List.of(
                    new MonthlyStatisticsResponse.MonthlySummaryDto(
                            "2024-01",
                            new BigDecimal("4000.00"),
                            new BigDecimal("-2000.00"),
                            new BigDecimal("2000.00")
                    )
            );

            // when
            var response = new MonthlyStatisticsResponse(TEST_YEAR, months);

            // then
            assertThat(response.year()).isEqualTo(TEST_YEAR);
            assertThat(response.months()).hasSize(1);
        }

        @Test
        @DisplayName("when two responses have same values then equals returns true")
        void given_same_values_when_equals_then_true() {
            // given
            var months = List.of(
                    new MonthlyStatisticsResponse.MonthlySummaryDto(
                            "2024-03",
                            new BigDecimal("6000.00"),
                            new BigDecimal("-3500.00"),
                            new BigDecimal("2500.00")
                    )
            );
            var response1 = new MonthlyStatisticsResponse(TEST_YEAR, months);
            var response2 = new MonthlyStatisticsResponse(TEST_YEAR, months);

            // then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("when full year data then all months accessible")
        void given_full_year_when_created_then_all_months_accessible() {
            // given
            var months = List.of(
                    new MonthlyStatisticsResponse.MonthlySummaryDto("2024-01", new BigDecimal("1000"), new BigDecimal("-500"), new BigDecimal("500")),
                    new MonthlyStatisticsResponse.MonthlySummaryDto("2024-02", new BigDecimal("1100"), new BigDecimal("-600"), new BigDecimal("500")),
                    new MonthlyStatisticsResponse.MonthlySummaryDto("2024-03", new BigDecimal("1200"), new BigDecimal("-700"), new BigDecimal("500")),
                    new MonthlyStatisticsResponse.MonthlySummaryDto("2024-04", new BigDecimal("1300"), new BigDecimal("-800"), new BigDecimal("500")),
                    new MonthlyStatisticsResponse.MonthlySummaryDto("2024-05", new BigDecimal("1400"), new BigDecimal("-900"), new BigDecimal("500")),
                    new MonthlyStatisticsResponse.MonthlySummaryDto("2024-06", new BigDecimal("1500"), new BigDecimal("-1000"), new BigDecimal("500")),
                    new MonthlyStatisticsResponse.MonthlySummaryDto("2024-07", new BigDecimal("1600"), new BigDecimal("-1100"), new BigDecimal("500")),
                    new MonthlyStatisticsResponse.MonthlySummaryDto("2024-08", new BigDecimal("1700"), new BigDecimal("-1200"), new BigDecimal("500")),
                    new MonthlyStatisticsResponse.MonthlySummaryDto("2024-09", new BigDecimal("1800"), new BigDecimal("-1300"), new BigDecimal("500")),
                    new MonthlyStatisticsResponse.MonthlySummaryDto("2024-10", new BigDecimal("1900"), new BigDecimal("-1400"), new BigDecimal("500")),
                    new MonthlyStatisticsResponse.MonthlySummaryDto("2024-11", new BigDecimal("2000"), new BigDecimal("-1500"), new BigDecimal("500")),
                    new MonthlyStatisticsResponse.MonthlySummaryDto("2024-12", new BigDecimal("2100"), new BigDecimal("-1600"), new BigDecimal("500"))
            );

            // when
            var response = new MonthlyStatisticsResponse(TEST_YEAR, months);

            // then
            assertThat(response.months()).hasSize(12);
            assertThat(response.months().get(0).month()).isEqualTo("2024-01");
            assertThat(response.months().get(11).month()).isEqualTo("2024-12");
        }
    }
}
