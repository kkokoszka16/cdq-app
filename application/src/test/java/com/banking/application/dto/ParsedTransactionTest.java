package com.banking.application.dto;

import com.banking.domain.model.Category;
import com.banking.domain.model.Iban;
import com.banking.domain.model.Money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ParsedTransaction")
class ParsedTransactionTest {

    private static final String VALID_IBAN = "PL61109010140000071219812874";
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 3, 15);
    private static final Currency PLN = Currency.getInstance("PLN");

    @Nested
    @DisplayName("given valid data")
    class GivenValidData {

        @Test
        @DisplayName("when created then stores all fields correctly")
        void given_valid_data_when_created_then_stores_fields() {
            // given
            var iban = Iban.of(VALID_IBAN);
            var date = TEST_DATE;
            var currency = PLN;
            var category = Category.FOOD;
            var amount = Money.of(new BigDecimal("100.50"));

            // when
            var parsed = new ParsedTransaction(iban, date, currency, category, amount);

            // then
            assertThat(parsed.iban()).isEqualTo(iban);
            assertThat(parsed.date()).isEqualTo(date);
            assertThat(parsed.currency()).isEqualTo(currency);
            assertThat(parsed.category()).isEqualTo(category);
            assertThat(parsed.amount()).isEqualTo(amount);
        }

        @Test
        @DisplayName("when created then iban returns Iban value object")
        void given_valid_data_when_iban_accessed_then_returns_value_object() {
            // given
            var iban = Iban.of(VALID_IBAN);
            var amount = Money.of(new BigDecimal("50.00"));
            var parsed = new ParsedTransaction(iban, TEST_DATE, PLN, Category.UTILITIES, amount);

            // then
            assertThat(parsed.iban()).isInstanceOf(Iban.class);
            assertThat(parsed.iban().value()).isEqualTo(VALID_IBAN);
        }

        @Test
        @DisplayName("when created then amount returns Money value object")
        void given_valid_data_when_amount_accessed_then_returns_value_object() {
            // given
            var expectedAmount = new BigDecimal("250.75");
            var iban = Iban.of(VALID_IBAN);
            var amount = Money.of(expectedAmount);
            var parsed = new ParsedTransaction(iban, TEST_DATE, PLN, Category.TRANSPORT, amount);

            // then
            assertThat(parsed.amount()).isInstanceOf(Money.class);
            assertThat(parsed.amount().amount()).isEqualByComparingTo(expectedAmount);
        }
    }

    @Nested
    @DisplayName("given different categories")
    class GivenDifferentCategories {

        @Test
        @DisplayName("when FOOD category then stored correctly")
        void given_food_category_when_created_then_stored() {
            // given
            var parsed = createParsedTransaction(Category.FOOD);

            // then
            assertThat(parsed.category()).isEqualTo(Category.FOOD);
        }

        @Test
        @DisplayName("when UTILITIES category then stored correctly")
        void given_utilities_category_when_created_then_stored() {
            // given
            var parsed = createParsedTransaction(Category.UTILITIES);

            // then
            assertThat(parsed.category()).isEqualTo(Category.UTILITIES);
        }

        @Test
        @DisplayName("when TRANSPORT category then stored correctly")
        void given_transport_category_when_created_then_stored() {
            // given
            var parsed = createParsedTransaction(Category.TRANSPORT);

            // then
            assertThat(parsed.category()).isEqualTo(Category.TRANSPORT);
        }

        private ParsedTransaction createParsedTransaction(Category category) {
            var iban = Iban.of(VALID_IBAN);
            var amount = Money.of(new BigDecimal("100.00"));
            return new ParsedTransaction(iban, TEST_DATE, PLN, category, amount);
        }
    }

    @Nested
    @DisplayName("given record equality")
    class GivenRecordEquality {

        @Test
        @DisplayName("when same data then equals returns true")
        void given_same_data_when_equals_then_true() {
            // given
            var iban = Iban.of(VALID_IBAN);
            var amount = Money.of(new BigDecimal("100.00"));

            var parsed1 = new ParsedTransaction(iban, TEST_DATE, PLN, Category.FOOD, amount);
            var parsed2 = new ParsedTransaction(iban, TEST_DATE, PLN, Category.FOOD, amount);

            // then
            assertThat(parsed1).isEqualTo(parsed2);
        }

        @Test
        @DisplayName("when different date then equals returns false")
        void given_different_date_when_equals_then_false() {
            // given
            var iban = Iban.of(VALID_IBAN);
            var amount = Money.of(new BigDecimal("100.00"));

            var parsed1 = new ParsedTransaction(iban, LocalDate.of(2024, 1, 1), PLN, Category.FOOD, amount);
            var parsed2 = new ParsedTransaction(iban, LocalDate.of(2024, 12, 31), PLN, Category.FOOD, amount);

            // then
            assertThat(parsed1).isNotEqualTo(parsed2);
        }

        @Test
        @DisplayName("when different category then equals returns false")
        void given_different_category_when_equals_then_false() {
            // given
            var iban = Iban.of(VALID_IBAN);
            var amount = Money.of(new BigDecimal("100.00"));

            var parsed1 = new ParsedTransaction(iban, TEST_DATE, PLN, Category.FOOD, amount);
            var parsed2 = new ParsedTransaction(iban, TEST_DATE, PLN, Category.UTILITIES, amount);

            // then
            assertThat(parsed1).isNotEqualTo(parsed2);
        }

        @Test
        @DisplayName("when same data then hashCode matches")
        void given_same_data_when_hashcode_then_matches() {
            // given
            var iban = Iban.of(VALID_IBAN);
            var amount = Money.of(new BigDecimal("100.00"));

            var parsed1 = new ParsedTransaction(iban, TEST_DATE, PLN, Category.FOOD, amount);
            var parsed2 = new ParsedTransaction(iban, TEST_DATE, PLN, Category.FOOD, amount);

            // then
            assertThat(parsed1.hashCode()).isEqualTo(parsed2.hashCode());
        }
    }

    @Nested
    @DisplayName("given different currencies")
    class GivenDifferentCurrencies {

        @Test
        @DisplayName("when EUR currency then stored correctly")
        void given_eur_currency_when_created_then_stored() {
            // given
            var eur = Currency.getInstance("EUR");
            var iban = Iban.of(VALID_IBAN);
            var amount = Money.of(new BigDecimal("100.00"));

            // when
            var parsed = new ParsedTransaction(iban, TEST_DATE, eur, Category.FOOD, amount);

            // then
            assertThat(parsed.currency()).isEqualTo(eur);
            assertThat(parsed.currency().getCurrencyCode()).isEqualTo("EUR");
        }

        @Test
        @DisplayName("when USD currency then stored correctly")
        void given_usd_currency_when_created_then_stored() {
            // given
            var usd = Currency.getInstance("USD");
            var iban = Iban.of(VALID_IBAN);
            var amount = Money.of(new BigDecimal("100.00"));

            // when
            var parsed = new ParsedTransaction(iban, TEST_DATE, usd, Category.FOOD, amount);

            // then
            assertThat(parsed.currency()).isEqualTo(usd);
            assertThat(parsed.currency().getCurrencyCode()).isEqualTo("USD");
        }
    }
}
