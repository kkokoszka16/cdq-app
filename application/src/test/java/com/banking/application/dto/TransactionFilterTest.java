package com.banking.application.dto;

import com.banking.domain.model.Category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionFilter")
class TransactionFilterTest {

    private static final String VALID_IBAN = "PL61109010140000071219812874";
    private static final Category VALID_CATEGORY = Category.FOOD;
    private static final LocalDate VALID_FROM = LocalDate.of(2024, 1, 1);
    private static final LocalDate VALID_TO = LocalDate.of(2024, 1, 31);

    @Nested
    @DisplayName("defaults")
    class Defaults {

        @Test
        @DisplayName("when creating defaults then has null filters and default pagination")
        void given_defaults_when_creating_then_has_expected_values() {
            // when
            var filter = TransactionFilter.defaults();

            // then
            assertThat(filter.iban()).isNull();
            assertThat(filter.category()).isNull();
            assertThat(filter.from()).isNull();
            assertThat(filter.to()).isNull();
            assertThat(filter.page()).isZero();
            assertThat(filter.size()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("page normalization")
    class PageNormalization {

        @ParameterizedTest(name = "page {0} should be normalized to 0")
        @ValueSource(ints = {-10, -1, 0})
        @DisplayName("when page is negative then normalized to zero")
        void given_negative_page_when_creating_then_normalized_to_zero(int page) {
            // when
            var filter = new TransactionFilter(null, null, null, null, page, 20);

            // then
            assertThat(filter.page()).isZero();
        }

        @Test
        @DisplayName("when page is positive then kept as is")
        void given_positive_page_when_creating_then_kept() {
            // when
            var filter = new TransactionFilter(null, null, null, null, 5, 20);

            // then
            assertThat(filter.page()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("size normalization")
    class SizeNormalization {

        @ParameterizedTest(name = "size {0} should be normalized to 1")
        @ValueSource(ints = {-10, 0})
        @DisplayName("when size is zero or negative then normalized to one")
        void given_zero_or_negative_size_when_creating_then_normalized_to_one(int size) {
            // when
            var filter = new TransactionFilter(null, null, null, null, 0, size);

            // then
            assertThat(filter.size()).isEqualTo(1);
        }

        @ParameterizedTest(name = "size {0} should be normalized to 100")
        @ValueSource(ints = {101, 150, 1000})
        @DisplayName("when size exceeds max then normalized to max")
        void given_size_exceeding_max_when_creating_then_normalized_to_max(int size) {
            // when
            var filter = new TransactionFilter(null, null, null, null, 0, size);

            // then
            assertThat(filter.size()).isEqualTo(100);
        }

        @ParameterizedTest(name = "size {0} should be kept as is")
        @CsvSource({"1, 1", "20, 20", "50, 50", "100, 100"})
        @DisplayName("when size is within range then kept as is")
        void given_valid_size_when_creating_then_kept(int inputSize, int expectedSize) {
            // when
            var filter = new TransactionFilter(null, null, null, null, 0, inputSize);

            // then
            assertThat(filter.size()).isEqualTo(expectedSize);
        }
    }

    @Nested
    @DisplayName("builder methods")
    class BuilderMethods {

        @Test
        @DisplayName("when using withIban then creates new filter with iban")
        void given_filter_when_with_iban_then_creates_new_with_iban() {
            // given
            var original = TransactionFilter.defaults();

            // when
            var withIban = original.withIban(VALID_IBAN);

            // then
            assertThat(withIban.iban()).isEqualTo(VALID_IBAN);
            assertThat(withIban.category()).isNull();
            assertThat(withIban.from()).isNull();
            assertThat(withIban.to()).isNull();
            assertThat(original.iban()).isNull();
        }

        @Test
        @DisplayName("when using withCategory then creates new filter with category")
        void given_filter_when_with_category_then_creates_new_with_category() {
            // given
            var original = TransactionFilter.defaults();

            // when
            var withCategory = original.withCategory(VALID_CATEGORY);

            // then
            assertThat(withCategory.category()).isEqualTo(VALID_CATEGORY);
            assertThat(withCategory.iban()).isNull();
            assertThat(original.category()).isNull();
        }

        @Test
        @DisplayName("when using withDateRange then creates new filter with date range")
        void given_filter_when_with_date_range_then_creates_new_with_dates() {
            // given
            var original = TransactionFilter.defaults();

            // when
            var withDates = original.withDateRange(VALID_FROM, VALID_TO);

            // then
            assertThat(withDates.from()).isEqualTo(VALID_FROM);
            assertThat(withDates.to()).isEqualTo(VALID_TO);
            assertThat(original.from()).isNull();
            assertThat(original.to()).isNull();
        }

        @Test
        @DisplayName("when using withPagination then creates new filter with pagination")
        void given_filter_when_with_pagination_then_creates_new_with_pagination() {
            // given
            var original = TransactionFilter.defaults();

            // when
            var withPagination = original.withPagination(3, 50);

            // then
            assertThat(withPagination.page()).isEqualTo(3);
            assertThat(withPagination.size()).isEqualTo(50);
            assertThat(original.page()).isZero();
            assertThat(original.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("when chaining builder methods then all modifications applied")
        void given_filter_when_chaining_methods_then_all_applied() {
            // when
            var filter = TransactionFilter.defaults()
                    .withIban(VALID_IBAN)
                    .withCategory(VALID_CATEGORY)
                    .withDateRange(VALID_FROM, VALID_TO)
                    .withPagination(2, 50);

            // then
            assertThat(filter.iban()).isEqualTo(VALID_IBAN);
            assertThat(filter.category()).isEqualTo(VALID_CATEGORY);
            assertThat(filter.from()).isEqualTo(VALID_FROM);
            assertThat(filter.to()).isEqualTo(VALID_TO);
            assertThat(filter.page()).isEqualTo(2);
            assertThat(filter.size()).isEqualTo(50);
        }
    }
}
