package com.banking.application.dto;

import com.banking.domain.model.Category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionPage")
class TransactionPageTest {

    private static final TransactionView SAMPLE_VIEW = new TransactionView(
            "tx-001",
            "PL61109010140000071219812874",
            LocalDate.of(2024, 1, 15),
            "PLN",
            Category.FOOD,
            new BigDecimal("-100.00"),
            "batch-001"
    );

    @Nested
    @DisplayName("factory method")
    class FactoryMethod {

        @Test
        @DisplayName("when creating page then all fields are set correctly")
        void given_valid_params_when_creating_then_fields_are_set() {
            // given
            var content = List.of(SAMPLE_VIEW);

            // when
            var page = TransactionPage.of(content, 0, 20, 100);

            // then
            assertThat(page.content()).isEqualTo(content);
            assertThat(page.page()).isZero();
            assertThat(page.size()).isEqualTo(20);
            assertThat(page.totalElements()).isEqualTo(100);
            assertThat(page.totalPages()).isEqualTo(5);
        }

        @ParameterizedTest(name = "totalElements={0}, size={1} -> totalPages={2}")
        @CsvSource({
                "0, 20, 0",
                "1, 20, 1",
                "19, 20, 1",
                "20, 20, 1",
                "21, 20, 2",
                "100, 20, 5",
                "101, 20, 6",
                "100, 10, 10",
                "99, 100, 1"
        })
        @DisplayName("when calculating total pages then uses ceiling division")
        void given_elements_and_size_when_calculating_pages_then_uses_ceiling_division(
                long totalElements, int size, int expectedPages) {
            // when
            var page = TransactionPage.of(List.of(), 0, size, totalElements);

            // then
            assertThat(page.totalPages()).isEqualTo(expectedPages);
        }
    }

    @Nested
    @DisplayName("navigation")
    class Navigation {

        @Test
        @DisplayName("when on first page with more pages then hasNext is true")
        void given_first_page_with_more_when_checking_next_then_has_next_is_true() {
            // when
            var page = TransactionPage.of(List.of(SAMPLE_VIEW), 0, 10, 100);

            // then
            assertThat(page.hasNext()).isTrue();
            assertThat(page.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("when on middle page then has both next and previous")
        void given_middle_page_when_checking_navigation_then_has_both_next_and_previous() {
            // when
            var page = TransactionPage.of(List.of(SAMPLE_VIEW), 5, 10, 100);

            // then
            assertThat(page.hasNext()).isTrue();
            assertThat(page.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("when on last page then hasNext is false")
        void given_last_page_when_checking_next_then_has_next_is_false() {
            // when
            var page = TransactionPage.of(List.of(SAMPLE_VIEW), 9, 10, 100);

            // then
            assertThat(page.hasNext()).isFalse();
            assertThat(page.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("when single page then has neither next nor previous")
        void given_single_page_when_checking_navigation_then_has_neither_next_nor_previous() {
            // when
            var page = TransactionPage.of(List.of(SAMPLE_VIEW), 0, 10, 5);

            // then
            assertThat(page.hasNext()).isFalse();
            assertThat(page.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("when empty result then has neither next nor previous")
        void given_empty_result_when_checking_navigation_then_has_neither_next_nor_previous() {
            // when
            var page = TransactionPage.of(List.of(), 0, 10, 0);

            // then
            assertThat(page.hasNext()).isFalse();
            assertThat(page.hasPrevious()).isFalse();
        }
    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        @DisplayName("when content is empty then isEmpty returns true")
        void given_empty_content_when_checking_empty_then_returns_true() {
            // when
            var page = TransactionPage.of(List.of(), 0, 10, 0);

            // then
            assertThat(page.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("when content has elements then isEmpty returns false")
        void given_content_with_elements_when_checking_empty_then_returns_false() {
            // when
            var page = TransactionPage.of(List.of(SAMPLE_VIEW), 0, 10, 1);

            // then
            assertThat(page.isEmpty()).isFalse();
        }
    }
}
