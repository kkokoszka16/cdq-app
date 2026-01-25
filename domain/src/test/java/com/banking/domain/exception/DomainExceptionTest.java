package com.banking.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DomainException")
class DomainExceptionTest {

    @Nested
    @DisplayName("given InvalidIbanException")
    class GivenInvalidIbanException {

        @Test
        @DisplayName("when created with message then returns message")
        void given_message_when_created_then_returns_message() {
            // given
            var message = "IBAN validation failed";

            // when
            var exception = new InvalidIbanException(message);

            // then
            assertThat(exception.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("when created then is instance of DomainException")
        void given_invalid_iban_exception_when_checked_then_is_domain_exception() {
            // given
            var exception = new InvalidIbanException("test");

            // then
            assertThat(exception).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("when created then is instance of RuntimeException")
        void given_invalid_iban_exception_when_checked_then_is_runtime_exception() {
            // given
            var exception = new InvalidIbanException("test");

            // then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("given InvalidAmountException")
    class GivenInvalidAmountException {

        @Test
        @DisplayName("when created with message then returns message")
        void given_message_when_created_then_returns_message() {
            // given
            var message = "Amount must be positive";

            // when
            var exception = new InvalidAmountException(message);

            // then
            assertThat(exception.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("when created then is instance of DomainException")
        void given_invalid_amount_exception_when_checked_then_is_domain_exception() {
            // given
            var exception = new InvalidAmountException("test");

            // then
            assertThat(exception).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("when created then is instance of RuntimeException")
        void given_invalid_amount_exception_when_checked_then_is_runtime_exception() {
            // given
            var exception = new InvalidAmountException("test");

            // then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("given InvalidTransactionException")
    class GivenInvalidTransactionException {

        @Test
        @DisplayName("when created with message then returns message")
        void given_message_when_created_then_returns_message() {
            // given
            var message = "Transaction validation failed";

            // when
            var exception = new InvalidTransactionException(message);

            // then
            assertThat(exception.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("when created then is instance of DomainException")
        void given_invalid_transaction_exception_when_checked_then_is_domain_exception() {
            // given
            var exception = new InvalidTransactionException("test");

            // then
            assertThat(exception).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("when created then is instance of RuntimeException")
        void given_invalid_transaction_exception_when_checked_then_is_runtime_exception() {
            // given
            var exception = new InvalidTransactionException("test");

            // then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("given sealed class hierarchy")
    class GivenSealedClassHierarchy {

        @Test
        @DisplayName("when checking permitted subclasses then contains all exception types")
        void given_domain_exception_when_checking_permitted_then_contains_all_types() {
            // given
            var permittedSubclasses = DomainException.class.getPermittedSubclasses();

            // then
            assertThat(permittedSubclasses)
                    .hasSize(3)
                    .containsExactlyInAnyOrder(
                            InvalidIbanException.class,
                            InvalidAmountException.class,
                            InvalidTransactionException.class
                    );
        }

        @Test
        @DisplayName("when checking DomainException then is sealed")
        void given_domain_exception_when_checking_then_is_sealed() {
            // then
            assertThat(DomainException.class.isSealed()).isTrue();
        }

        @Test
        @DisplayName("when checking subclasses then are final")
        void given_subclasses_when_checking_then_are_final() {
            // then
            assertThat(InvalidIbanException.class.getModifiers() & java.lang.reflect.Modifier.FINAL)
                    .isNotZero();
            assertThat(InvalidAmountException.class.getModifiers() & java.lang.reflect.Modifier.FINAL)
                    .isNotZero();
            assertThat(InvalidTransactionException.class.getModifiers() & java.lang.reflect.Modifier.FINAL)
                    .isNotZero();
        }
    }
}
