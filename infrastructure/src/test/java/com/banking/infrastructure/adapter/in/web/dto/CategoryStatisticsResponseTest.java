package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.CategoryStatistics;
import com.banking.application.dto.CategoryStatistics.CategorySummary;
import com.banking.domain.model.Category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryStatisticsResponse")
class CategoryStatisticsResponseTest {

    private static final YearMonth TEST_MONTH = YearMonth.of(2024, 3);

    @Nested
    @DisplayName("given CategoryStatistics with data")
    class GivenCategoryStatisticsWithData {

        @Test
        @DisplayName("when from called then maps all fields correctly")
        void given_statistics_when_from_then_maps_correctly() {
            // given
            var summaries = List.of(
                    new CategorySummary(Category.SALARY, new BigDecimal("5000.00"), 5),
                    new CategorySummary(Category.FOOD, new BigDecimal("800.00"), 12),
                    new CategorySummary(Category.ENTERTAINMENT, new BigDecimal("200.00"), 3)
            );
            var statistics = new CategoryStatistics(TEST_MONTH, summaries);

            // when
            var response = CategoryStatisticsResponse.from(statistics);

            // then
            assertThat(response.month()).isEqualTo("2024-03");
            assertThat(response.categories()).hasSize(3);
        }

        @Test
        @DisplayName("when from called then maps category summaries correctly")
        void given_statistics_when_from_then_maps_summaries() {
            // given
            var summaries = List.of(
                    new CategorySummary(Category.SALARY, new BigDecimal("5000.00"), 5)
            );
            var statistics = new CategoryStatistics(TEST_MONTH, summaries);

            // when
            var response = CategoryStatisticsResponse.from(statistics);

            // then
            var categoryDto = response.categories().get(0);
            assertThat(categoryDto.category()).isEqualTo(Category.SALARY);
            assertThat(categoryDto.totalAmount()).isEqualByComparingTo("5000.00");
            assertThat(categoryDto.transactionCount()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("given empty CategoryStatistics")
    class GivenEmptyCategoryStatistics {

        @Test
        @DisplayName("when from called then returns response with empty categories")
        void given_empty_statistics_when_from_then_empty_categories() {
            // given
            var statistics = CategoryStatistics.empty(TEST_MONTH);

            // when
            var response = CategoryStatisticsResponse.from(statistics);

            // then
            assertThat(response.month()).isEqualTo("2024-03");
            assertThat(response.categories()).isEmpty();
        }
    }

    @Nested
    @DisplayName("given CategorySummaryDto record")
    class GivenCategorySummaryDto {

        @Test
        @DisplayName("when created then all fields accessible")
        void given_dto_when_created_then_fields_accessible() {
            // given
            var category = Category.UTILITIES;
            var totalAmount = new BigDecimal("350.00");
            var transactionCount = 8L;

            // when
            var dto = new CategoryStatisticsResponse.CategorySummaryDto(
                    category,
                    totalAmount,
                    transactionCount
            );

            // then
            assertThat(dto.category()).isEqualTo(category);
            assertThat(dto.totalAmount()).isEqualByComparingTo("350.00");
            assertThat(dto.transactionCount()).isEqualTo(8L);
        }

        @Test
        @DisplayName("when two dtos have same values then equals returns true")
        void given_same_values_when_equals_then_true() {
            // given
            var dto1 = new CategoryStatisticsResponse.CategorySummaryDto(
                    Category.TRANSFER,
                    new BigDecimal("100.00"),
                    2
            );
            var dto2 = new CategoryStatisticsResponse.CategorySummaryDto(
                    Category.TRANSFER,
                    new BigDecimal("100.00"),
                    2
            );

            // then
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }
    }

    @Nested
    @DisplayName("given CategoryStatisticsResponse record")
    class GivenCategoryStatisticsResponseRecord {

        @Test
        @DisplayName("when created then all fields accessible")
        void given_response_when_created_then_fields_accessible() {
            // given
            var categories = List.of(
                    new CategoryStatisticsResponse.CategorySummaryDto(
                            Category.OTHER,
                            new BigDecimal("50.00"),
                            1
                    )
            );

            // when
            var response = new CategoryStatisticsResponse("2024-06", categories);

            // then
            assertThat(response.month()).isEqualTo("2024-06");
            assertThat(response.categories()).hasSize(1);
        }

        @Test
        @DisplayName("when two responses have same values then equals returns true")
        void given_same_values_when_equals_then_true() {
            // given
            var categories = List.of(
                    new CategoryStatisticsResponse.CategorySummaryDto(
                            Category.HEALTHCARE,
                            new BigDecimal("75.00"),
                            3
                    )
            );
            var response1 = new CategoryStatisticsResponse("2024-01", categories);
            var response2 = new CategoryStatisticsResponse("2024-01", categories);

            // then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }
    }
}
