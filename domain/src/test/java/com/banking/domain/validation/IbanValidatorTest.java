package com.banking.domain.validation;

import com.banking.domain.exception.InvalidIbanException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("IbanValidator")
class IbanValidatorTest {

    private static final String VALID_POLISH_IBAN = "PL61109010140000071219812874";
    private static final String VALID_GERMAN_IBAN = "DE89370400440532013000";
    private static final String INVALID_IBAN = "INVALID_IBAN";

    @Nested
    @DisplayName("isValid")
    class IsValid {

        @Test
        @DisplayName("when validating valid Polish IBAN then returns true")
        void given_valid_polish_iban_when_validating_then_returns_true() {
            // when
            var result = IbanValidator.isValid(VALID_POLISH_IBAN);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("when validating valid German IBAN then returns true")
        void given_valid_german_iban_when_validating_then_returns_true() {
            // when
            var result = IbanValidator.isValid(VALID_GERMAN_IBAN);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("when validating IBAN with spaces then returns true")
        void given_iban_with_spaces_when_validating_then_returns_true() {
            // given
            var ibanWithSpaces = "PL61 1090 1014 0000 0712 1981 2874";

            // when
            var result = IbanValidator.isValid(ibanWithSpaces);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("when validating lowercase IBAN then returns true")
        void given_lowercase_iban_when_validating_then_returns_true() {
            // given
            var lowercaseIban = "pl61109010140000071219812874";

            // when
            var result = IbanValidator.isValid(lowercaseIban);

            // then
            assertThat(result).isTrue();
        }

        @ParameterizedTest(name = "value: [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("when validating null/empty/blank then returns false")
        void given_null_empty_blank_when_validating_then_returns_false(String value) {
            // when
            var result = IbanValidator.isValid(value);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("when validating invalid format then returns false")
        void given_invalid_format_when_validating_then_returns_false() {
            // when
            var result = IbanValidator.isValid(INVALID_IBAN);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("when validating IBAN with invalid checksum then returns false")
        void given_invalid_checksum_when_validating_then_returns_false() {
            // given
            var invalidChecksum = "PL00109010140000071219812874";

            // when
            var result = IbanValidator.isValid(invalidChecksum);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("when validating too short IBAN then returns false")
        void given_too_short_iban_when_validating_then_returns_false() {
            // given
            var shortIban = "PL123456";

            // when
            var result = IbanValidator.isValid(shortIban);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("tryParse")
    class TryParse {

        @Test
        @DisplayName("when parsing valid IBAN then returns optional with IBAN")
        void given_valid_iban_when_parsing_then_returns_present_optional() {
            // when
            var result = IbanValidator.tryParse(VALID_POLISH_IBAN);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().value()).isEqualTo(VALID_POLISH_IBAN);
        }

        @Test
        @DisplayName("when parsing IBAN with spaces then returns optional with normalized IBAN")
        void given_iban_with_spaces_when_parsing_then_returns_normalized() {
            // given
            var ibanWithSpaces = "PL61 1090 1014 0000 0712 1981 2874";

            // when
            var result = IbanValidator.tryParse(ibanWithSpaces);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().value()).isEqualTo(VALID_POLISH_IBAN);
        }

        @Test
        @DisplayName("when parsing invalid IBAN then returns empty optional")
        void given_invalid_iban_when_parsing_then_returns_empty() {
            // when
            var result = IbanValidator.tryParse(INVALID_IBAN);

            // then
            assertThat(result).isEmpty();
        }

        @ParameterizedTest(name = "value: [{0}]")
        @NullAndEmptySource
        @DisplayName("when parsing null/empty then returns empty optional")
        void given_null_or_empty_when_parsing_then_returns_empty(String value) {
            // when
            var result = IbanValidator.tryParse(value);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("parseOrThrow")
    class ParseOrThrow {

        @Test
        @DisplayName("when parsing valid IBAN then returns IBAN")
        void given_valid_iban_when_parsing_then_returns_iban() {
            // given
            var contextMessage = "Account IBAN validation";

            // when
            var result = IbanValidator.parseOrThrow(VALID_POLISH_IBAN, contextMessage);

            // then
            assertThat(result.value()).isEqualTo(VALID_POLISH_IBAN);
        }

        @Test
        @DisplayName("when parsing invalid IBAN then throws exception with context")
        void given_invalid_iban_when_parsing_then_throws_with_context() {
            // given
            var contextMessage = "Sender IBAN validation";

            // when/then
            assertThatThrownBy(() -> IbanValidator.parseOrThrow(INVALID_IBAN, contextMessage))
                    .isInstanceOf(InvalidIbanException.class)
                    .hasMessageContaining(contextMessage);
        }

        @Test
        @DisplayName("when parsing null IBAN then throws exception with context")
        void given_null_iban_when_parsing_then_throws_with_context() {
            // given
            var contextMessage = "Recipient IBAN validation";

            // when/then
            assertThatThrownBy(() -> IbanValidator.parseOrThrow(null, contextMessage))
                    .isInstanceOf(InvalidIbanException.class)
                    .hasMessageContaining(contextMessage);
        }

        @Test
        @DisplayName("when parsing IBAN with invalid checksum then throws exception with context")
        void given_invalid_checksum_when_parsing_then_throws_with_context() {
            // given
            var invalidChecksum = "PL00109010140000071219812874";
            var contextMessage = "Payment validation";

            // when/then
            assertThatThrownBy(() -> IbanValidator.parseOrThrow(invalidChecksum, contextMessage))
                    .isInstanceOf(InvalidIbanException.class)
                    .hasMessageContaining(contextMessage)
                    .hasMessageContaining("checksum");
        }
    }
}
