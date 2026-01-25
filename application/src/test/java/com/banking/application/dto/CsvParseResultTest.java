package com.banking.application.dto;

import com.banking.domain.model.Category;
import com.banking.domain.model.Iban;
import com.banking.domain.model.Money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CsvParseResult")
class CsvParseResultTest {

    private static final ParsedTransaction SAMPLE_TRANSACTION = new ParsedTransaction(
            Iban.of("PL61109010140000071219812874"),
            LocalDate.of(2024, 1, 15),
            Currency.getInstance("PLN"),
            Category.FOOD,
            Money.of("-100.00")
    );

    private static final CsvParseResult.ParseError SAMPLE_ERROR =
            new CsvParseResult.ParseError(5, "Invalid IBAN");

    @Nested
    @DisplayName("counts")
    class Counts {

        @Test
        @DisplayName("when result has only valid transactions then successCount matches")
        void given_valid_transactions_when_checking_success_count_then_matches() {
            // given
            var transactions = List.of(SAMPLE_TRANSACTION, SAMPLE_TRANSACTION, SAMPLE_TRANSACTION);
            var result = new CsvParseResult(transactions, List.of(), 3);

            // then
            assertThat(result.successCount()).isEqualTo(3);
            assertThat(result.errorCount()).isZero();
        }

        @Test
        @DisplayName("when result has only errors then errorCount matches")
        void given_errors_when_checking_error_count_then_matches() {
            // given
            var errors = List.of(SAMPLE_ERROR, SAMPLE_ERROR);
            var result = new CsvParseResult(List.of(), errors, 2);

            // then
            assertThat(result.errorCount()).isEqualTo(2);
            assertThat(result.successCount()).isZero();
        }

        @Test
        @DisplayName("when result has mixed content then both counts are correct")
        void given_mixed_content_when_checking_counts_then_both_correct() {
            // given
            var transactions = List.of(SAMPLE_TRANSACTION, SAMPLE_TRANSACTION);
            var errors = List.of(SAMPLE_ERROR);
            var result = new CsvParseResult(transactions, errors, 3);

            // then
            assertThat(result.successCount()).isEqualTo(2);
            assertThat(result.errorCount()).isEqualTo(1);
            assertThat(result.totalRowsProcessed()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("hasErrors")
    class HasErrors {

        @Test
        @DisplayName("when result has no errors then hasErrors returns false")
        void given_no_errors_when_checking_has_errors_then_returns_false() {
            // given
            var result = new CsvParseResult(List.of(SAMPLE_TRANSACTION), List.of(), 1);

            // then
            assertThat(result.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("when result has errors then hasErrors returns true")
        void given_errors_when_checking_has_errors_then_returns_true() {
            // given
            var result = new CsvParseResult(List.of(), List.of(SAMPLE_ERROR), 1);

            // then
            assertThat(result.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("when result is completely empty then hasErrors returns false")
        void given_empty_result_when_checking_has_errors_then_returns_false() {
            // given
            var result = new CsvParseResult(List.of(), List.of(), 0);

            // then
            assertThat(result.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("ParseError")
    class ParseErrorTest {

        @Test
        @DisplayName("when creating error then fields are set correctly")
        void given_valid_params_when_creating_then_fields_set() {
            // when
            var error = new CsvParseResult.ParseError(10, "Column missing");

            // then
            assertThat(error.rowNumber()).isEqualTo(10);
            assertThat(error.message()).isEqualTo("Column missing");
        }

        @Test
        @DisplayName("when creating error with row zero then succeeds")
        void given_zero_row_when_creating_then_succeeds() {
            // when
            var error = new CsvParseResult.ParseError(0, "Header error");

            // then
            assertThat(error.rowNumber()).isZero();
        }
    }
}
