package com.banking.domain.model;

import com.banking.domain.exception.InvalidTransactionException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Transaction")
class TransactionTest {

    private static final TransactionId VALID_ID = TransactionId.generate();
    private static final Iban VALID_IBAN = Iban.of("PL61109010140000071219812874");
    private static final LocalDate VALID_DATE = LocalDate.now().minusDays(1);
    private static final Currency VALID_CURRENCY = Currency.getInstance("PLN");
    private static final Category VALID_CATEGORY = Category.FOOD;
    private static final Money VALID_AMOUNT = Money.of("-100.00");
    private static final String VALID_BATCH_ID = "batch-001";

    @Nested
    @DisplayName("given valid parameters")
    class GivenValidParameters {

        @Test
        @DisplayName("when creating transaction then all fields are set correctly")
        void given_valid_params_when_creating_then_fields_are_set() {
            // when
            var transaction = new Transaction(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    VALID_CATEGORY,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            );

            // then
            assertThat(transaction.id()).isEqualTo(VALID_ID);
            assertThat(transaction.iban()).isEqualTo(VALID_IBAN);
            assertThat(transaction.transactionDate()).isEqualTo(VALID_DATE);
            assertThat(transaction.currency()).isEqualTo(VALID_CURRENCY);
            assertThat(transaction.category()).isEqualTo(VALID_CATEGORY);
            assertThat(transaction.amount()).isEqualTo(VALID_AMOUNT);
            assertThat(transaction.importBatchId()).isEqualTo(VALID_BATCH_ID);
        }

        @Test
        @DisplayName("when creating with positive amount then isIncome returns true")
        void given_positive_amount_when_checking_income_then_returns_true() {
            // given
            var positiveAmount = Money.of("5000.00");

            // when
            var transaction = createTransaction(positiveAmount);

            // then
            assertThat(transaction.isIncome()).isTrue();
            assertThat(transaction.isExpense()).isFalse();
        }

        @Test
        @DisplayName("when creating with negative amount then isExpense returns true")
        void given_negative_amount_when_checking_expense_then_returns_true() {
            // given
            var negativeAmount = Money.of("-100.00");

            // when
            var transaction = createTransaction(negativeAmount);

            // then
            assertThat(transaction.isExpense()).isTrue();
            assertThat(transaction.isIncome()).isFalse();
        }

        @Test
        @DisplayName("when getting year then returns correct year from date")
        void given_transaction_when_getting_year_then_returns_correct_year() {
            // given
            var date = LocalDate.of(2024, 6, 15);

            // when
            var transaction = createTransactionWithDate(date);

            // then
            assertThat(transaction.getYear()).isEqualTo(2024);
        }

        @Test
        @DisplayName("when getting month then returns correct month from date")
        void given_transaction_when_getting_month_then_returns_correct_month() {
            // given
            var date = LocalDate.of(2024, 6, 15);

            // when
            var transaction = createTransactionWithDate(date);

            // then
            assertThat(transaction.getMonth()).isEqualTo(6);
        }

        @Test
        @DisplayName("when creating with today's date then succeeds")
        void given_today_date_when_creating_then_succeeds() {
            // given
            var today = LocalDate.now();

            // when
            var transaction = createTransactionWithDate(today);

            // then
            assertThat(transaction.transactionDate()).isEqualTo(today);
        }

        @Test
        @DisplayName("when creating with date exactly 10 years ago then succeeds")
        void given_date_exactly_10_years_ago_when_creating_then_succeeds() {
            // given
            var tenYearsAgo = LocalDate.now().minusYears(10).plusDays(1);

            // when
            var transaction = createTransactionWithDate(tenYearsAgo);

            // then
            assertThat(transaction.transactionDate()).isEqualTo(tenYearsAgo);
        }
    }

    @Nested
    @DisplayName("given null parameters")
    class GivenNullParameters {

        @Test
        @DisplayName("when transaction id is null then throws exception")
        void given_null_id_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> new Transaction(
                    null,
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    VALID_CATEGORY,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            ))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("Transaction ID is required");
        }

        @Test
        @DisplayName("when iban is null then throws exception")
        void given_null_iban_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> new Transaction(
                    VALID_ID,
                    null,
                    VALID_DATE,
                    VALID_CURRENCY,
                    VALID_CATEGORY,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            ))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("IBAN is required");
        }

        @Test
        @DisplayName("when date is null then throws exception")
        void given_null_date_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> new Transaction(
                    VALID_ID,
                    VALID_IBAN,
                    null,
                    VALID_CURRENCY,
                    VALID_CATEGORY,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            ))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("Transaction date is required");
        }

        @Test
        @DisplayName("when currency is null then throws exception")
        void given_null_currency_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> new Transaction(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    null,
                    VALID_CATEGORY,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            ))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("Currency is required");
        }

        @Test
        @DisplayName("when category is null then throws exception")
        void given_null_category_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> new Transaction(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    null,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            ))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("Category is required");
        }

        @Test
        @DisplayName("when amount is null then throws exception")
        void given_null_amount_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> new Transaction(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    VALID_CATEGORY,
                    null,
                    VALID_BATCH_ID
            ))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("Amount is required");
        }

        @ParameterizedTest(name = "batchId: [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("when batch id is null/empty/blank then throws exception")
        void given_null_empty_blank_batch_id_when_creating_then_throws_exception(String batchId) {
            // when/then
            assertThatThrownBy(() -> new Transaction(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    VALID_CATEGORY,
                    VALID_AMOUNT,
                    batchId
            ))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("Import batch ID is required");
        }
    }

    @Nested
    @DisplayName("given invalid date")
    class GivenInvalidDate {

        @Test
        @DisplayName("when date is in the future then throws exception")
        void given_future_date_when_creating_then_throws_exception() {
            // given
            var futureDate = LocalDate.now().plusDays(1);

            // when/then
            assertThatThrownBy(() -> new Transaction(
                    VALID_ID,
                    VALID_IBAN,
                    futureDate,
                    VALID_CURRENCY,
                    VALID_CATEGORY,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            ))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("cannot be in the future");
        }

        @Test
        @DisplayName("when date is more than 10 years old then throws exception")
        void given_date_too_old_when_creating_then_throws_exception() {
            // given
            var veryOldDate = LocalDate.now().minusYears(11);

            // when/then
            assertThatThrownBy(() -> new Transaction(
                    VALID_ID,
                    VALID_IBAN,
                    veryOldDate,
                    VALID_CURRENCY,
                    VALID_CATEGORY,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            ))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("cannot be older than 10 years");
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("when comparing same transaction then equals returns true")
        void given_same_transaction_when_comparing_then_returns_true() {
            // given
            var transaction1 = new Transaction(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    VALID_CATEGORY,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            );
            var transaction2 = new Transaction(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    VALID_CATEGORY,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            );

            // when/then
            assertThat(transaction1).isEqualTo(transaction2);
            assertThat(transaction1.hashCode()).isEqualTo(transaction2.hashCode());
        }

        @Test
        @DisplayName("when comparing different transactions then equals returns false")
        void given_different_transactions_when_comparing_then_returns_false() {
            // given
            var transaction1 = createTransaction(VALID_AMOUNT);
            var transaction2 = new Transaction(
                    TransactionId.generate(),
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    VALID_CATEGORY,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            );

            // when/then
            assertThat(transaction1).isNotEqualTo(transaction2);
        }
    }

    private Transaction createTransaction(Money amount) {
        return new Transaction(
                VALID_ID,
                VALID_IBAN,
                VALID_DATE,
                VALID_CURRENCY,
                VALID_CATEGORY,
                amount,
                VALID_BATCH_ID
        );
    }

    private Transaction createTransactionWithDate(LocalDate date) {
        return new Transaction(
                VALID_ID,
                VALID_IBAN,
                date,
                VALID_CURRENCY,
                VALID_CATEGORY,
                VALID_AMOUNT,
                VALID_BATCH_ID
        );
    }
}
