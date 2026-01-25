package com.banking.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TransactionId")
class TransactionIdTest {

    @Nested
    @DisplayName("given valid value")
    class GivenValidValue {

        @Test
        @DisplayName("when creating with UUID string then succeeds")
        void given_uuid_string_when_creating_then_succeeds() {
            // given
            var uuidValue = "550e8400-e29b-41d4-a716-446655440000";

            // when
            var transactionId = TransactionId.of(uuidValue);

            // then
            assertThat(transactionId.value()).isEqualTo(uuidValue);
        }

        @Test
        @DisplayName("when creating with arbitrary string then succeeds")
        void given_arbitrary_string_when_creating_then_succeeds() {
            // given
            var arbitraryValue = "tx-12345-abc";

            // when
            var transactionId = TransactionId.of(arbitraryValue);

            // then
            assertThat(transactionId.value()).isEqualTo(arbitraryValue);
        }

        @Test
        @DisplayName("when generating then creates unique id")
        void given_generation_when_generating_then_creates_unique_id() {
            // when
            var transactionId1 = TransactionId.generate();
            var transactionId2 = TransactionId.generate();

            // then
            assertThat(transactionId1.value()).isNotBlank();
            assertThat(transactionId2.value()).isNotBlank();
            assertThat(transactionId1).isNotEqualTo(transactionId2);
        }

        @Test
        @DisplayName("when calling toString then returns value")
        void given_transaction_id_when_to_string_then_returns_value() {
            // given
            var value = "test-id-123";
            var transactionId = TransactionId.of(value);

            // when
            var result = transactionId.toString();

            // then
            assertThat(result).isEqualTo(value);
        }
    }

    @Nested
    @DisplayName("given invalid value")
    class GivenInvalidValue {

        @ParameterizedTest(name = "value: [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("when creating from null/empty/blank then throws exception")
        void given_null_empty_blank_when_creating_then_throws_exception(String value) {
            // when/then
            assertThatThrownBy(() -> TransactionId.of(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or blank");
        }

        @Test
        @DisplayName("when using constructor directly with null then throws exception")
        void given_null_in_constructor_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> new TransactionId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or blank");
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("when comparing equal ids then equals returns true")
        void given_equal_ids_when_comparing_then_returns_true() {
            // given
            var value = "same-id-123";
            var id1 = TransactionId.of(value);
            var id2 = TransactionId.of(value);

            // when/then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("when comparing different ids then equals returns false")
        void given_different_ids_when_comparing_then_returns_false() {
            // given
            var id1 = TransactionId.of("id-1");
            var id2 = TransactionId.of("id-2");

            // when/then
            assertThat(id1).isNotEqualTo(id2);
        }
    }
}
