package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.TransactionView;
import com.banking.domain.model.Category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TransactionDto.
 */
@DisplayName("TransactionDto")
class TransactionDtoTest {

    private static final String VALID_ID = "tx-dto-test-123";
    private static final String VALID_IBAN = "PL61109010140000071219812874";
    private static final LocalDate VALID_DATE = LocalDate.of(2024, 6, 15);
    private static final String VALID_CURRENCY = "PLN";
    private static final BigDecimal VALID_AMOUNT = new BigDecimal("150.50");
    private static final String VALID_BATCH_ID = "batch-123";

    @Nested
    @DisplayName("given record constructor")
    class GivenRecordConstructor {

        @Test
        @DisplayName("when created with valid values then all fields are set")
        void given_valid_values_when_created_then_all_fields_set() {
            // when
            var dto = new TransactionDto(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    Category.FOOD,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            );

            // then
            assertThat(dto.id()).isEqualTo(VALID_ID);
            assertThat(dto.iban()).isEqualTo(VALID_IBAN);
            assertThat(dto.transactionDate()).isEqualTo(VALID_DATE);
            assertThat(dto.currency()).isEqualTo(VALID_CURRENCY);
            assertThat(dto.category()).isEqualTo(Category.FOOD);
            assertThat(dto.amount()).isEqualByComparingTo(VALID_AMOUNT);
            assertThat(dto.importBatchId()).isEqualTo(VALID_BATCH_ID);
        }

        @Test
        @DisplayName("when created with negative amount then preserves negative value")
        void given_negative_amount_when_created_then_preserves_negative() {
            // given
            var negativeAmount = new BigDecimal("-99.99");

            // when
            var dto = new TransactionDto(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    Category.SHOPPING,
                    negativeAmount,
                    VALID_BATCH_ID
            );

            // then
            assertThat(dto.amount()).isNegative();
            assertThat(dto.amount()).isEqualByComparingTo(negativeAmount);
        }

        @ParameterizedTest
        @EnumSource(Category.class)
        @DisplayName("when created with any category then stores category")
        void given_any_category_when_created_then_stores_category(Category category) {
            // when
            var dto = new TransactionDto(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    category,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            );

            // then
            assertThat(dto.category()).isEqualTo(category);
        }

        @ParameterizedTest
        @ValueSource(strings = {"USD", "EUR", "GBP", "CHF"})
        @DisplayName("when created with various currencies then stores currency code")
        void given_various_currencies_when_created_then_stores_currency(String currency) {
            // when
            var dto = new TransactionDto(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    currency,
                    Category.TRANSFER,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            );

            // then
            assertThat(dto.currency()).isEqualTo(currency);
        }
    }

    @Nested
    @DisplayName("given from factory method")
    class GivenFromFactoryMethod {

        @Test
        @DisplayName("when converting from TransactionView then maps all fields")
        void given_transaction_view_when_from_then_maps_all_fields() {
            // given
            var view = new TransactionView(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    Category.SALARY,
                    new BigDecimal("5000.00"),
                    VALID_BATCH_ID
            );

            // when
            var dto = TransactionDto.from(view);

            // then
            assertThat(dto.id()).isEqualTo(VALID_ID);
            assertThat(dto.iban()).isEqualTo(VALID_IBAN);
            assertThat(dto.transactionDate()).isEqualTo(VALID_DATE);
            assertThat(dto.currency()).isEqualTo(VALID_CURRENCY);
            assertThat(dto.category()).isEqualTo(Category.SALARY);
            assertThat(dto.amount()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(dto.importBatchId()).isEqualTo(VALID_BATCH_ID);
        }

        @Test
        @DisplayName("when converting expense view then preserves negative amount")
        void given_expense_view_when_from_then_preserves_negative() {
            // given
            var view = new TransactionView(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    Category.UTILITIES,
                    new BigDecimal("-250.75"),
                    VALID_BATCH_ID
            );

            // when
            var dto = TransactionDto.from(view);

            // then
            assertThat(dto.amount()).isNegative();
            assertThat(dto.amount()).isEqualByComparingTo(new BigDecimal("-250.75"));
        }

        @ParameterizedTest
        @EnumSource(Category.class)
        @DisplayName("when converting view with any category then maps category")
        void given_any_category_when_from_then_maps_category(Category category) {
            // given
            var view = new TransactionView(
                    VALID_ID,
                    VALID_IBAN,
                    VALID_DATE,
                    VALID_CURRENCY,
                    category,
                    VALID_AMOUNT,
                    VALID_BATCH_ID
            );

            // when
            var dto = TransactionDto.from(view);

            // then
            assertThat(dto.category()).isEqualTo(category);
        }
    }
}
