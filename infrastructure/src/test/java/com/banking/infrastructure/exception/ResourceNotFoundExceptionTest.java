package com.banking.infrastructure.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResourceNotFoundException")
class ResourceNotFoundExceptionTest {

    @Nested
    @DisplayName("given resource type and id")
    class GivenResourceTypeAndId {

        @Test
        @DisplayName("when created then stores resource type")
        void given_resource_type_when_created_then_stores_type() {
            // given
            var resourceType = "ImportBatch";
            var resourceId = "123";

            // when
            var exception = new ResourceNotFoundException(resourceType, resourceId);

            // then
            assertThat(exception.getResourceType()).isEqualTo(resourceType);
        }

        @Test
        @DisplayName("when created then stores resource id")
        void given_resource_id_when_created_then_stores_id() {
            // given
            var resourceType = "ImportBatch";
            var resourceId = "abc-123-def";

            // when
            var exception = new ResourceNotFoundException(resourceType, resourceId);

            // then
            assertThat(exception.getResourceId()).isEqualTo(resourceId);
        }

        @Test
        @DisplayName("when created then constructs descriptive message")
        void given_type_and_id_when_created_then_constructs_message() {
            // given
            var resourceType = "Transaction";
            var resourceId = "tx-456";

            // when
            var exception = new ResourceNotFoundException(resourceType, resourceId);

            // then
            assertThat(exception.getMessage())
                    .isEqualTo("Transaction not found: tx-456");
        }

        @ParameterizedTest
        @CsvSource({
                "ImportBatch, batch-001, ImportBatch not found: batch-001",
                "Transaction, tx-123, Transaction not found: tx-123",
                "User, user-999, User not found: user-999"
        })
        @DisplayName("when created with different types then message is formatted correctly")
        void given_various_types_when_created_then_message_correct(
                String resourceType,
                String resourceId,
                String expectedMessage
        ) {
            // when
            var exception = new ResourceNotFoundException(resourceType, resourceId);

            // then
            assertThat(exception.getMessage()).isEqualTo(expectedMessage);
        }
    }

    @Nested
    @DisplayName("given exception hierarchy")
    class GivenExceptionHierarchy {

        @Test
        @DisplayName("when checked then is RuntimeException")
        void given_exception_when_checked_then_is_runtime_exception() {
            // given
            var exception = new ResourceNotFoundException("Test", "1");

            // then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
