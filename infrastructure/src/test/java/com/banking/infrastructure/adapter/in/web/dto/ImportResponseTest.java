package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.ImportResult;
import com.banking.domain.model.ImportStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ImportResponse DTO.
 */
@DisplayName("ImportResponse")
class ImportResponseTest {

    private static final String VALID_IMPORT_ID = "import-test-123";
    private static final String VALID_MESSAGE = "Import started";

    @Nested
    @DisplayName("given record constructor")
    class GivenRecordConstructor {

        @Test
        @DisplayName("when created with valid values then all fields are set")
        void given_valid_values_when_created_then_all_fields_set() {
            // when
            var response = new ImportResponse(VALID_IMPORT_ID, ImportStatus.PROCESSING, VALID_MESSAGE);

            // then
            assertThat(response.importId()).isEqualTo(VALID_IMPORT_ID);
            assertThat(response.status()).isEqualTo(ImportStatus.PROCESSING);
            assertThat(response.message()).isEqualTo(VALID_MESSAGE);
        }

        @ParameterizedTest
        @EnumSource(ImportStatus.class)
        @DisplayName("when created with any status then stores status")
        void given_any_status_when_created_then_stores_status(ImportStatus status) {
            // when
            var response = new ImportResponse(VALID_IMPORT_ID, status, VALID_MESSAGE);

            // then
            assertThat(response.status()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("given from factory method")
    class GivenFromFactoryMethod {

        @Test
        @DisplayName("when converting from ImportResult then maps all fields")
        void given_import_result_when_from_then_maps_all_fields() {
            // given
            var importResult = new ImportResult(VALID_IMPORT_ID, ImportStatus.PENDING, "Import queued");

            // when
            var response = ImportResponse.from(importResult);

            // then
            assertThat(response.importId()).isEqualTo(VALID_IMPORT_ID);
            assertThat(response.status()).isEqualTo(ImportStatus.PENDING);
            assertThat(response.message()).isEqualTo("Import queued");
        }

        @Test
        @DisplayName("when converting completed result then maps completed status")
        void given_completed_result_when_from_then_maps_completed() {
            // given
            var importResult = new ImportResult(
                    "batch-complete",
                    ImportStatus.COMPLETED,
                    "Import finished successfully"
            );

            // when
            var response = ImportResponse.from(importResult);

            // then
            assertThat(response.status()).isEqualTo(ImportStatus.COMPLETED);
            assertThat(response.message()).isEqualTo("Import finished successfully");
        }

        @Test
        @DisplayName("when converting failed result then maps failed status")
        void given_failed_result_when_from_then_maps_failed() {
            // given
            var importResult = new ImportResult(
                    "batch-failed",
                    ImportStatus.FAILED,
                    "Import failed due to invalid data"
            );

            // when
            var response = ImportResponse.from(importResult);

            // then
            assertThat(response.status()).isEqualTo(ImportStatus.FAILED);
            assertThat(response.message()).contains("failed");
        }
    }
}
