package com.banking.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IbanStatistics")
class IbanStatisticsTest {

    private static final YearMonth TEST_MONTH = YearMonth.of(2024, 1);
    private static final String POLISH_IBAN = "PL61109010140000071219812874";

    @Nested
    @DisplayName("empty factory")
    class EmptyFactory {

        @Test
        @DisplayName("when creating empty statistics then month is set and ibans are empty")
        void given_month_when_creating_empty_then_ibans_empty() {
            // when
            var stats = IbanStatistics.empty(TEST_MONTH);

            // then
            assertThat(stats.month()).isEqualTo(TEST_MONTH);
            assertThat(stats.ibans()).isEmpty();
        }
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("when creating with ibans then all fields are set")
        void given_ibans_when_creating_then_fields_set() {
            // given
            var summaries = List.of(
                    new IbanStatistics.IbanSummary(POLISH_IBAN, new BigDecimal("5000.00"), new BigDecimal("-500.00")),
                    new IbanStatistics.IbanSummary("DE89370400440532013000", new BigDecimal("3000.00"), new BigDecimal("-300.00"))
            );

            // when
            var stats = new IbanStatistics(TEST_MONTH, summaries);

            // then
            assertThat(stats.month()).isEqualTo(TEST_MONTH);
            assertThat(stats.ibans()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("IbanSummary")
    class IbanSummaryTest {

        @Test
        @DisplayName("when creating with income and expense then balance is calculated")
        void given_income_and_expense_when_creating_then_balance_calculated() {
            // when
            var summary = new IbanStatistics.IbanSummary(
                    POLISH_IBAN,
                    new BigDecimal("5000.00"),
                    new BigDecimal("-500.00")
            );

            // then
            assertThat(summary.iban()).isEqualTo(POLISH_IBAN);
            assertThat(summary.totalIncome()).isEqualByComparingTo("5000.00");
            assertThat(summary.totalExpense()).isEqualByComparingTo("-500.00");
            assertThat(summary.balance()).isEqualByComparingTo("4500.00");
        }

        @Test
        @DisplayName("when creating with zero income then balance equals expense")
        void given_zero_income_when_creating_then_balance_equals_expense() {
            // when
            var summary = new IbanStatistics.IbanSummary(
                    POLISH_IBAN,
                    BigDecimal.ZERO,
                    new BigDecimal("-1000.00")
            );

            // then
            assertThat(summary.balance()).isEqualByComparingTo("-1000.00");
        }

        @Test
        @DisplayName("when creating with zero expense then balance equals income")
        void given_zero_expense_when_creating_then_balance_equals_income() {
            // when
            var summary = new IbanStatistics.IbanSummary(
                    POLISH_IBAN,
                    new BigDecimal("3000.00"),
                    BigDecimal.ZERO
            );

            // then
            assertThat(summary.balance()).isEqualByComparingTo("3000.00");
        }

        @Test
        @DisplayName("when creating with full constructor then all fields set")
        void given_all_params_when_creating_then_all_fields_set() {
            // when
            var summary = new IbanStatistics.IbanSummary(
                    POLISH_IBAN,
                    new BigDecimal("5000.00"),
                    new BigDecimal("-500.00"),
                    new BigDecimal("4500.00")
            );

            // then
            assertThat(summary.iban()).isEqualTo(POLISH_IBAN);
            assertThat(summary.totalIncome()).isEqualByComparingTo("5000.00");
            assertThat(summary.totalExpense()).isEqualByComparingTo("-500.00");
            assertThat(summary.balance()).isEqualByComparingTo("4500.00");
        }
    }
}
