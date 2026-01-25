package com.banking.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MonthlyStatistics")
class MonthlyStatisticsTest {

    private static final int TEST_YEAR = 2024;

    @Nested
    @DisplayName("empty factory")
    class EmptyFactory {

        @Test
        @DisplayName("when creating empty statistics then year is set and months are empty")
        void given_year_when_creating_empty_then_months_empty() {
            // when
            var stats = MonthlyStatistics.empty(TEST_YEAR);

            // then
            assertThat(stats.year()).isEqualTo(TEST_YEAR);
            assertThat(stats.months()).isEmpty();
        }
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("when creating with months then all fields are set")
        void given_months_when_creating_then_fields_set() {
            // given
            var summaries = List.of(
                    new MonthlyStatistics.MonthlySummary(
                            YearMonth.of(2024, 1),
                            new BigDecimal("5000.00"),
                            new BigDecimal("-3000.00")
                    ),
                    new MonthlyStatistics.MonthlySummary(
                            YearMonth.of(2024, 2),
                            new BigDecimal("5500.00"),
                            new BigDecimal("-2800.00")
                    )
            );

            // when
            var stats = new MonthlyStatistics(TEST_YEAR, summaries);

            // then
            assertThat(stats.year()).isEqualTo(TEST_YEAR);
            assertThat(stats.months()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("MonthlySummary")
    class MonthlySummaryTest {

        @Test
        @DisplayName("when creating with income and expense then balance is calculated")
        void given_income_and_expense_when_creating_then_balance_calculated() {
            // given
            var month = YearMonth.of(2024, 3);

            // when
            var summary = new MonthlyStatistics.MonthlySummary(
                    month,
                    new BigDecimal("6000.00"),
                    new BigDecimal("-4000.00")
            );

            // then
            assertThat(summary.month()).isEqualTo(month);
            assertThat(summary.totalIncome()).isEqualByComparingTo("6000.00");
            assertThat(summary.totalExpense()).isEqualByComparingTo("-4000.00");
            assertThat(summary.balance()).isEqualByComparingTo("2000.00");
        }

        @Test
        @DisplayName("when creating with only expenses then balance is negative")
        void given_only_expenses_when_creating_then_balance_negative() {
            // when
            var summary = new MonthlyStatistics.MonthlySummary(
                    YearMonth.of(2024, 4),
                    BigDecimal.ZERO,
                    new BigDecimal("-1500.00")
            );

            // then
            assertThat(summary.balance()).isEqualByComparingTo("-1500.00");
        }

        @Test
        @DisplayName("when creating with only income then balance equals income")
        void given_only_income_when_creating_then_balance_equals_income() {
            // when
            var summary = new MonthlyStatistics.MonthlySummary(
                    YearMonth.of(2024, 5),
                    new BigDecimal("5000.00"),
                    BigDecimal.ZERO
            );

            // then
            assertThat(summary.balance()).isEqualByComparingTo("5000.00");
        }

        @Test
        @DisplayName("when creating with full constructor then all fields set")
        void given_all_params_when_creating_then_all_fields_set() {
            // given
            var month = YearMonth.of(2024, 6);

            // when
            var summary = new MonthlyStatistics.MonthlySummary(
                    month,
                    new BigDecimal("7000.00"),
                    new BigDecimal("-3500.00"),
                    new BigDecimal("3500.00")
            );

            // then
            assertThat(summary.month()).isEqualTo(month);
            assertThat(summary.totalIncome()).isEqualByComparingTo("7000.00");
            assertThat(summary.totalExpense()).isEqualByComparingTo("-3500.00");
            assertThat(summary.balance()).isEqualByComparingTo("3500.00");
        }

        @Test
        @DisplayName("when expense exceeds income then balance is negative")
        void given_expense_exceeds_income_when_creating_then_balance_negative() {
            // when
            var summary = new MonthlyStatistics.MonthlySummary(
                    YearMonth.of(2024, 7),
                    new BigDecimal("2000.00"),
                    new BigDecimal("-3000.00")
            );

            // then
            assertThat(summary.balance()).isEqualByComparingTo("-1000.00");
        }
    }
}
