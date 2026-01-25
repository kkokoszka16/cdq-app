package com.banking.application.dto;

import com.banking.domain.model.Category;
import com.banking.domain.model.Iban;
import com.banking.domain.model.Money;
import com.banking.domain.model.Transaction;
import com.banking.domain.model.TransactionId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionView")
class TransactionViewTest {

    private static final String BATCH_ID = "batch-001";
    private static final String POLISH_IBAN = "PL61109010140000071219812874";

    @Nested
    @DisplayName("from transaction")
    class FromTransaction {

        @Test
        @DisplayName("when converting transaction then all fields are mapped")
        void given_transaction_when_converting_then_all_fields_are_mapped() {
            // given
            var transactionId = TransactionId.generate();
            var iban = Iban.of(POLISH_IBAN);
            var date = LocalDate.of(2024, 1, 15);
            var currency = Currency.getInstance("PLN");
            var category = Category.FOOD;
            var amount = Money.of("-125.50");

            var transaction = new Transaction(
                    transactionId,
                    iban,
                    date,
                    currency,
                    category,
                    amount,
                    BATCH_ID
            );

            // when
            var view = TransactionView.from(transaction);

            // then
            assertThat(view.id()).isEqualTo(transactionId.value());
            assertThat(view.iban()).isEqualTo(POLISH_IBAN);
            assertThat(view.transactionDate()).isEqualTo(date);
            assertThat(view.currency()).isEqualTo("PLN");
            assertThat(view.category()).isEqualTo(Category.FOOD);
            assertThat(view.amount()).isEqualByComparingTo("-125.50");
            assertThat(view.importBatchId()).isEqualTo(BATCH_ID);
        }

        @Test
        @DisplayName("when converting income transaction then amount is positive")
        void given_income_transaction_when_converting_then_amount_is_positive() {
            // given
            var transaction = createTransaction(Money.of("5000.00"), Category.SALARY);

            // when
            var view = TransactionView.from(transaction);

            // then
            assertThat(view.amount()).isPositive();
            assertThat(view.category()).isEqualTo(Category.SALARY);
        }

        @Test
        @DisplayName("when converting expense transaction then amount is negative")
        void given_expense_transaction_when_converting_then_amount_is_negative() {
            // given
            var transaction = createTransaction(Money.of("-75.25"), Category.TRANSPORT);

            // when
            var view = TransactionView.from(transaction);

            // then
            assertThat(view.amount()).isNegative();
            assertThat(view.category()).isEqualTo(Category.TRANSPORT);
        }

        @Test
        @DisplayName("when converting transaction with different currency then currency code is correct")
        void given_eur_transaction_when_converting_then_currency_code_is_eur() {
            // given
            var transaction = new Transaction(
                    TransactionId.generate(),
                    Iban.of("DE89370400440532013000"),
                    LocalDate.of(2024, 1, 15),
                    Currency.getInstance("EUR"),
                    Category.SHOPPING,
                    Money.of("-200.00"),
                    BATCH_ID
            );

            // when
            var view = TransactionView.from(transaction);

            // then
            assertThat(view.currency()).isEqualTo("EUR");
            assertThat(view.iban()).isEqualTo("DE89370400440532013000");
        }
    }

    private Transaction createTransaction(Money amount, Category category) {
        return new Transaction(
                TransactionId.generate(),
                Iban.of(POLISH_IBAN),
                LocalDate.of(2024, 1, 15),
                Currency.getInstance("PLN"),
                category,
                amount,
                BATCH_ID
        );
    }
}
