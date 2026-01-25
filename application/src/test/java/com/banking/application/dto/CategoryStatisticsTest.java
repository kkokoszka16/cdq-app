package com.banking.application.dto;

import com.banking.domain.model.Category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryStatistics")
class CategoryStatisticsTest {

    private static final YearMonth TEST_MONTH = YearMonth.of(2024, 1);

    @Nested
    @DisplayName("empty factory")
    class EmptyFactory {

        @Test
        @DisplayName("when creating empty statistics then month is set and categories are empty")
        void given_month_when_creating_empty_then_categories_empty() {
            // when
            var stats = CategoryStatistics.empty(TEST_MONTH);

            // then
            assertThat(stats.month()).isEqualTo(TEST_MONTH);
            assertThat(stats.categories()).isEmpty();
        }
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("when creating with categories then all fields are set")
        void given_categories_when_creating_then_fields_set() {
            // given
            var summaries = List.of(
                    new CategoryStatistics.CategorySummary(Category.FOOD, new BigDecimal("-500.00"), 5),
                    new CategoryStatistics.CategorySummary(Category.TRANSPORT, new BigDecimal("-150.00"), 3)
            );

            // when
            var stats = new CategoryStatistics(TEST_MONTH, summaries);

            // then
            assertThat(stats.month()).isEqualTo(TEST_MONTH);
            assertThat(stats.categories()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("CategorySummary")
    class CategorySummaryTest {

        @Test
        @DisplayName("when creating summary then all fields are set")
        void given_valid_params_when_creating_then_fields_set() {
            // when
            var summary = new CategoryStatistics.CategorySummary(
                    Category.SALARY,
                    new BigDecimal("5000.00"),
                    1
            );

            // then
            assertThat(summary.category()).isEqualTo(Category.SALARY);
            assertThat(summary.totalAmount()).isEqualByComparingTo("5000.00");
            assertThat(summary.transactionCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("when creating summary with negative amount then represents expense")
        void given_negative_amount_when_creating_then_represents_expense() {
            // when
            var summary = new CategoryStatistics.CategorySummary(
                    Category.FOOD,
                    new BigDecimal("-750.50"),
                    10
            );

            // then
            assertThat(summary.totalAmount()).isNegative();
            assertThat(summary.transactionCount()).isEqualTo(10);
        }
    }
}
