package com.banking.domain.model;

import com.banking.domain.exception.InvalidAmountException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money")
class MoneyTest {

    @Nested
    @DisplayName("given valid amount")
    class GivenValidAmount {

        @Test
        @DisplayName("when creating from positive value then succeeds")
        void given_positive_amount_when_creating_then_succeeds() {
            // given
            var value = new BigDecimal("100.50");

            // when
            var money = Money.of(value);

            // then
            assertThat(money.amount()).isEqualByComparingTo("100.50");
        }

        @Test
        @DisplayName("when creating from negative value then succeeds")
        void given_negative_amount_when_creating_then_succeeds() {
            // given
            var value = new BigDecimal("-50.25");

            // when
            var money = Money.of(value);

            // then
            assertThat(money.amount()).isEqualByComparingTo("-50.25");
        }

        @Test
        @DisplayName("when creating from string then parses correctly")
        void given_string_amount_when_creating_then_parses_correctly() {
            // given
            var stringValue = "123.45";

            // when
            var money = Money.of(stringValue);

            // then
            assertThat(money.amount()).isEqualByComparingTo("123.45");
        }

        @ParameterizedTest
        @CsvSource({
                "100.999, 101.00",
                "100.994, 100.99",
                "100.995, 101.00"
        })
        @DisplayName("when creating with extra decimals then rounds to two places")
        void given_extra_decimals_when_creating_then_rounds(String input, String expected) {
            // when
            var money = Money.of(input);

            // then
            assertThat(money.amount()).isEqualByComparingTo(expected);
        }
    }

    @Nested
    @DisplayName("given invalid amount")
    class GivenInvalidAmount {

        @Test
        @DisplayName("when creating from null then throws exception")
        void given_null_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> Money.of((BigDecimal) null))
                    .isInstanceOf(InvalidAmountException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("when creating from zero then throws exception")
        void given_zero_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> Money.of(BigDecimal.ZERO))
                    .isInstanceOf(InvalidAmountException.class)
                    .hasMessageContaining("zero");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"abc", "12.34.56", "1,234.56"})
        @DisplayName("when creating from invalid string then throws exception")
        void given_invalid_string_when_creating_then_throws_exception(String value) {
            // when/then
            assertThatThrownBy(() -> Money.of(value))
                    .isInstanceOf(InvalidAmountException.class);
        }
    }

    @Nested
    @DisplayName("operations")
    class Operations {

        @Test
        @DisplayName("when checking positive then returns true for positive amount")
        void given_positive_amount_when_checking_positive_then_returns_true() {
            // given
            var money = Money.of("100.00");

            // when/then
            assertThat(money.isPositive()).isTrue();
            assertThat(money.isNegative()).isFalse();
        }

        @Test
        @DisplayName("when checking negative then returns true for negative amount")
        void given_negative_amount_when_checking_negative_then_returns_true() {
            // given
            var money = Money.of("-100.00");

            // when/then
            assertThat(money.isNegative()).isTrue();
            assertThat(money.isPositive()).isFalse();
        }

        @Test
        @DisplayName("when adding money then returns sum")
        void given_two_money_values_when_adding_then_returns_sum() {
            // given
            var money1 = Money.of("100.50");
            var money2 = Money.of("50.25");

            // when
            var result = money1.add(money2);

            // then
            assertThat(result.amount()).isEqualByComparingTo("150.75");
        }

        @Test
        @DisplayName("when negating then returns negated value")
        void given_positive_money_when_negating_then_returns_negative() {
            // given
            var money = Money.of("100.00");

            // when
            var result = money.negate();

            // then
            assertThat(result.amount()).isEqualByComparingTo("-100.00");
        }
    }
}
