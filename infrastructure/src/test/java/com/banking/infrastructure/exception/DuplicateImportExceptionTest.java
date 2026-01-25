package com.banking.infrastructure.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DuplicateImportException")
class DuplicateImportExceptionTest {

    @Nested
    @DisplayName("given existing import id and message")
    class GivenExistingImportIdAndMessage {

        @Test
        @DisplayName("when created then stores existing import id")
        void given_existing_id_when_created_then_stores_id() {
            // given
            var existingImportId = "import-123";
            var message = "File already imported";

            // when
            var exception = new DuplicateImportException(existingImportId, message);

            // then
            assertThat(exception.getExistingImportId()).isEqualTo(existingImportId);
        }

        @Test
        @DisplayName("when created then stores message")
        void given_message_when_created_then_stores_message() {
            // given
            var existingImportId = "import-456";
            var message = "Duplicate file detected: test.csv";

            // when
            var exception = new DuplicateImportException(existingImportId, message);

            // then
            assertThat(exception.getMessage()).isEqualTo(message);
        }

        @ParameterizedTest
        @CsvSource({
                "id-001, File already processed",
                "batch-xyz, Duplicate checksum found",
                "import-99, This file has been imported before"
        })
        @DisplayName("when created with various data then stores correctly")
        void given_various_data_when_created_then_stores_correctly(
                String existingId,
                String message
        ) {
            // when
            var exception = new DuplicateImportException(existingId, message);

            // then
            assertThat(exception.getExistingImportId()).isEqualTo(existingId);
            assertThat(exception.getMessage()).isEqualTo(message);
        }
    }

    @Nested
    @DisplayName("given exception hierarchy")
    class GivenExceptionHierarchy {

        @Test
        @DisplayName("when checked then is RuntimeException")
        void given_exception_when_checked_then_is_runtime_exception() {
            // given
            var exception = new DuplicateImportException("id", "message");

            // then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
