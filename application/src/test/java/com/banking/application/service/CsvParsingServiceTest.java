package com.banking.application.service;

import com.banking.domain.model.Category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CsvParsingService")
class CsvParsingServiceTest {

    private static final String VALID_HEADER = "iban,date,currency,category,amount";
    private static final String VALID_POLISH_IBAN = "PL61109010140000071219812874";
    private static final String VALID_GERMAN_IBAN = "DE89370400440532013000";

    private CsvParsingService parsingService;

    @BeforeEach
    void setUp() {
        parsingService = new CsvParsingService();
    }

    @Nested
    @DisplayName("given valid CSV content")
    class GivenValidCsv {

        @Test
        @DisplayName("when parsing single valid row then returns one transaction")
        void given_single_valid_row_when_parsing_then_returns_one_transaction() {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + ",2024-01-15,PLN,FOOD,-125.50"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).hasSize(1);
            assertThat(result.errors()).isEmpty();

            var transaction = result.validTransactions().getFirst();
            assertThat(transaction.iban().value()).isEqualTo(VALID_POLISH_IBAN);
            assertThat(transaction.date()).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(transaction.currency().getCurrencyCode()).isEqualTo("PLN");
            assertThat(transaction.category()).isEqualTo(Category.FOOD);
            assertThat(transaction.amount().amount()).isEqualByComparingTo("-125.50");
        }

        @Test
        @DisplayName("when parsing multiple valid rows then returns all transactions")
        void given_multiple_valid_rows_when_parsing_then_returns_all_transactions() {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + ",2024-01-15,PLN,FOOD,-125.50",
                    VALID_GERMAN_IBAN + ",2024-01-20,EUR,SALARY,5000.00",
                    VALID_POLISH_IBAN + ",2024-01-25,PLN,TRANSPORT,-45.00"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).hasSize(3);
            assertThat(result.errors()).isEmpty();
            assertThat(result.totalRowsProcessed()).isEqualTo(3);
        }

        @ParameterizedTest(name = "category {0} should be parsed correctly")
        @ValueSource(strings = {"FOOD", "TRANSPORT", "UTILITIES", "ENTERTAINMENT", "HEALTHCARE", "SHOPPING", "SALARY", "TRANSFER", "OTHER"})
        @DisplayName("when parsing all valid categories then succeeds")
        void given_valid_category_when_parsing_then_succeeds(String category) {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + ",2024-01-15,PLN," + category + ",-100.00"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).hasSize(1);
            assertThat(result.validTransactions().getFirst().category())
                    .isEqualTo(Category.valueOf(category));
        }

        @Test
        @DisplayName("when parsing lowercase category then normalizes correctly")
        void given_lowercase_category_when_parsing_then_normalizes() {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + ",2024-01-15,PLN,food,-100.00"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).hasSize(1);
            assertThat(result.validTransactions().getFirst().category()).isEqualTo(Category.FOOD);
        }

        @ParameterizedTest(name = "currency {0} should be parsed correctly")
        @ValueSource(strings = {"PLN", "EUR", "USD", "GBP", "CHF"})
        @DisplayName("when parsing valid currencies then succeeds")
        void given_valid_currency_when_parsing_then_succeeds(String currency) {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + ",2024-01-15," + currency + ",FOOD,-100.00"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).hasSize(1);
            assertThat(result.validTransactions().getFirst().currency().getCurrencyCode())
                    .isEqualTo(currency);
        }

        @Test
        @DisplayName("when parsing positive amount then returns income transaction")
        void given_positive_amount_when_parsing_then_returns_income() {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + ",2024-01-15,PLN,SALARY,5000.00"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions().getFirst().amount().isPositive()).isTrue();
        }

        @Test
        @DisplayName("when parsing negative amount then returns expense transaction")
        void given_negative_amount_when_parsing_then_returns_expense() {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + ",2024-01-15,PLN,FOOD,-125.50"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions().getFirst().amount().isNegative()).isTrue();
        }
    }

    @Nested
    @DisplayName("given CSV with edge cases")
    class GivenEdgeCases {

        @Test
        @DisplayName("when parsing file with UTF-8 BOM then strips BOM")
        void given_file_with_bom_when_parsing_then_strips_bom() {
            // given
            var bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            var csvContent = (VALID_HEADER + "\n" + VALID_POLISH_IBAN + ",2024-01-15,PLN,FOOD,-100.00");
            var csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);
            var contentWithBom = new byte[bom.length + csvBytes.length];
            System.arraycopy(bom, 0, contentWithBom, 0, bom.length);
            System.arraycopy(csvBytes, 0, contentWithBom, bom.length, csvBytes.length);

            // when
            var result = parsingService.parse(contentWithBom);

            // then
            assertThat(result.validTransactions()).hasSize(1);
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("when parsing file with empty rows then skips empty rows")
        void given_file_with_empty_rows_when_parsing_then_skips_empty_rows() {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    "",
                    VALID_POLISH_IBAN + ",2024-01-15,PLN,FOOD,-100.00",
                    "   ",
                    VALID_POLISH_IBAN + ",2024-01-16,PLN,TRANSPORT,-50.00",
                    ""
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).hasSize(2);
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("when parsing file with extra columns then ignores extra columns")
        void given_file_with_extra_columns_when_parsing_then_ignores_extras() {
            // given
            var csv = createCsv(
                    VALID_HEADER + ",extra1,extra2",
                    VALID_POLISH_IBAN + ",2024-01-15,PLN,FOOD,-100.00,ignore,this"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).hasSize(1);
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("when parsing file with quoted values containing commas then handles correctly")
        void given_quoted_values_with_commas_when_parsing_then_handles_correctly() {
            // given
            var csv = (VALID_HEADER + "\n\"" + VALID_POLISH_IBAN + "\",2024-01-15,PLN,FOOD,-100.00")
                    .getBytes(StandardCharsets.UTF_8);

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).hasSize(1);
        }

        @Test
        @DisplayName("when parsing file with whitespace around values then trims values")
        void given_whitespace_around_values_when_parsing_then_trims() {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    "  " + VALID_POLISH_IBAN + "  , 2024-01-15 , PLN , FOOD , -100.00 "
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).hasSize(1);
            assertThat(result.validTransactions().getFirst().iban().value()).isEqualTo(VALID_POLISH_IBAN);
        }

        @Test
        @DisplayName("when parsing file with CRLF line endings then parses correctly")
        void given_crlf_line_endings_when_parsing_then_parses_correctly() {
            // given
            var csvContent = VALID_HEADER + "\r\n" +
                    VALID_POLISH_IBAN + ",2024-01-15,PLN,FOOD,-100.00\r\n" +
                    VALID_POLISH_IBAN + ",2024-01-16,PLN,TRANSPORT,-50.00\r\n";

            // when
            var result = parsingService.parse(csvContent.getBytes(StandardCharsets.UTF_8));

            // then
            assertThat(result.validTransactions()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("given invalid CSV content")
    class GivenInvalidCsv {

        @Test
        @DisplayName("when parsing empty file then returns error")
        void given_empty_file_when_parsing_then_returns_error() {
            // given
            var csv = "".getBytes(StandardCharsets.UTF_8);

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).isEmpty();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().message()).contains("empty");
        }

        @Test
        @DisplayName("when parsing file with only header then returns no transactions")
        void given_only_header_when_parsing_then_returns_no_transactions() {
            // given
            var csv = createCsv(VALID_HEADER);

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).isEmpty();
            assertThat(result.errors()).isEmpty();
            assertThat(result.totalRowsProcessed()).isZero();
        }

        @Test
        @DisplayName("when parsing row with insufficient columns then reports error")
        void given_insufficient_columns_when_parsing_then_reports_error() {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + ",2024-01-15,PLN"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).isEmpty();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().rowNumber()).isEqualTo(1);
            assertThat(result.errors().getFirst().message()).contains("Insufficient columns");
        }

        @Test
        @DisplayName("when parsing invalid IBAN then reports error")
        void given_invalid_iban_when_parsing_then_reports_error() {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    "INVALID_IBAN,2024-01-15,PLN,FOOD,-100.00"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).isEmpty();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().message()).containsIgnoringCase("iban");
        }

        @ParameterizedTest(name = "invalid date {0} should be rejected")
        @CsvSource({
                "invalid-date, Invalid date format",
                "2024-13-01, Invalid date format",
                "2024-01-32, Invalid date format"
        })
        @DisplayName("when parsing invalid date format then reports error")
        void given_invalid_date_format_when_parsing_then_reports_error(String invalidDate, String expectedMessage) {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + "," + invalidDate + ",PLN,FOOD,-100.00"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).isEmpty();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().message()).containsIgnoringCase("date");
        }

        @Test
        @DisplayName("when parsing future date then reports error")
        void given_future_date_when_parsing_then_reports_error() {
            // given
            var futureDate = LocalDate.now().plusDays(1);
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + "," + futureDate + ",PLN,FOOD,-100.00"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).isEmpty();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().message()).contains("future");
        }

        @Test
        @DisplayName("when parsing very old date then reports error")
        void given_very_old_date_when_parsing_then_reports_error() {
            // given
            var oldDate = LocalDate.now().minusYears(11);
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + "," + oldDate + ",PLN,FOOD,-100.00"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).isEmpty();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().message()).contains("older than");
        }

        @Test
        @DisplayName("when parsing invalid currency then reports error")
        void given_invalid_currency_when_parsing_then_reports_error() {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + ",2024-01-15,INVALID,FOOD,-100.00"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).isEmpty();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().message()).containsIgnoringCase("currency");
        }

        @Test
        @DisplayName("when parsing unknown category then reports error")
        void given_unknown_category_when_parsing_then_reports_error() {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + ",2024-01-15,PLN,UNKNOWN_CATEGORY,-100.00"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).isEmpty();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().message()).containsIgnoringCase("category");
        }

        @ParameterizedTest(name = "invalid amount {0} should be rejected")
        @CsvSource({
                "abc, Invalid amount",
                "0, zero",
                "'', required"
        })
        @DisplayName("when parsing invalid amount then reports error")
        void given_invalid_amount_when_parsing_then_reports_error(String invalidAmount, String expectedSubstring) {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + ",2024-01-15,PLN,FOOD," + invalidAmount
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).isEmpty();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().message()).containsIgnoringCase(expectedSubstring);
        }
    }

    @Nested
    @DisplayName("given mixed valid and invalid rows")
    class GivenMixedContent {

        @Test
        @DisplayName("when parsing mixed content then processes valid rows and reports errors")
        void given_mixed_content_when_parsing_then_processes_valid_and_reports_errors() {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + ",2024-01-15,PLN,FOOD,-100.00",
                    "INVALID_IBAN,2024-01-16,PLN,FOOD,-50.00",
                    VALID_POLISH_IBAN + ",2024-01-17,PLN,TRANSPORT,-30.00",
                    VALID_POLISH_IBAN + ",invalid-date,PLN,FOOD,-20.00",
                    VALID_POLISH_IBAN + ",2024-01-19,PLN,SALARY,5000.00"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.validTransactions()).hasSize(3);
            assertThat(result.errors()).hasSize(2);
            assertThat(result.totalRowsProcessed()).isEqualTo(5);
            assertThat(result.successCount()).isEqualTo(3);
            assertThat(result.errorCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("when parsing then error row numbers are correct")
        void given_errors_in_rows_when_parsing_then_row_numbers_are_correct() {
            // given
            var csv = createCsv(
                    VALID_HEADER,
                    VALID_POLISH_IBAN + ",2024-01-15,PLN,FOOD,-100.00",
                    "INVALID_IBAN,2024-01-16,PLN,FOOD,-50.00",
                    VALID_POLISH_IBAN + ",2024-01-17,PLN,TRANSPORT,-30.00"
            );

            // when
            var result = parsingService.parse(csv);

            // then
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().rowNumber()).isEqualTo(2);
        }
    }

    private byte[] createCsv(String... lines) {
        return String.join("\n", lines).getBytes(StandardCharsets.UTF_8);
    }
}
