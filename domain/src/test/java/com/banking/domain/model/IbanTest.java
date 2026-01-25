package com.banking.domain.model;

import com.banking.domain.exception.InvalidIbanException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Iban")
class IbanTest {

    @Nested
    @DisplayName("given valid IBAN")
    class GivenValidIban {

        @Test
        @DisplayName("when creating from Polish IBAN then succeeds")
        void given_valid_polish_iban_when_creating_then_succeeds() {
            // given
            var ibanValue = "PL61109010140000071219812874";

            // when
            var iban = Iban.of(ibanValue);

            // then
            assertThat(iban.value()).isEqualTo(ibanValue);
        }

        @Test
        @DisplayName("when creating from German IBAN then succeeds")
        void given_valid_german_iban_when_creating_then_succeeds() {
            // given
            var ibanValue = "DE89370400440532013000";

            // when
            var iban = Iban.of(ibanValue);

            // then
            assertThat(iban.value()).isEqualTo(ibanValue);
        }

        @Test
        @DisplayName("when creating with lowercase then normalizes to uppercase")
        void given_lowercase_iban_when_creating_then_normalizes_to_uppercase() {
            // given
            var lowercaseIban = "pl61109010140000071219812874";

            // when
            var iban = Iban.of(lowercaseIban);

            // then
            assertThat(iban.value()).isEqualTo("PL61109010140000071219812874");
        }

        @Test
        @DisplayName("when creating with spaces then removes spaces")
        void given_iban_with_spaces_when_creating_then_removes_spaces() {
            // given
            var ibanWithSpaces = "PL61 1090 1014 0000 0712 1981 2874";

            // when
            var iban = Iban.of(ibanWithSpaces);

            // then
            assertThat(iban.value()).isEqualTo("PL61109010140000071219812874");
        }
    }

    @Nested
    @DisplayName("given invalid IBAN")
    class GivenInvalidIban {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("when creating from null/empty then throws exception")
        void given_null_or_empty_when_creating_then_throws_exception(String value) {
            // when/then
            assertThatThrownBy(() -> Iban.of(value))
                    .isInstanceOf(InvalidIbanException.class);
        }

        @Test
        @DisplayName("when creating from too short value then throws exception")
        void given_too_short_iban_when_creating_then_throws_exception() {
            // given
            var shortIban = "PL123456789";

            // when/then
            assertThatThrownBy(() -> Iban.of(shortIban))
                    .isInstanceOf(InvalidIbanException.class)
                    .hasMessageContaining("between");
        }

        @Test
        @DisplayName("when creating from invalid format then throws exception")
        void given_invalid_format_when_creating_then_throws_exception() {
            // given
            var invalidFormat = "123456789012345678901234";

            // when/then
            assertThatThrownBy(() -> Iban.of(invalidFormat))
                    .isInstanceOf(InvalidIbanException.class)
                    .hasMessageContaining("Invalid IBAN format");
        }

        @Test
        @DisplayName("when creating with invalid checksum then throws exception")
        void given_invalid_checksum_when_creating_then_throws_exception() {
            // given
            var invalidChecksum = "PL00109010140000071219812874";

            // when/then
            assertThatThrownBy(() -> Iban.of(invalidChecksum))
                    .isInstanceOf(InvalidIbanException.class)
                    .hasMessageContaining("checksum");
        }
    }
}
