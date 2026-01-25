package com.banking.application.dto;

import com.banking.domain.model.ImportStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ImportResult")
class ImportResultTest {

    private static final String IMPORT_ID = "import-001";

    @Nested
    @DisplayName("factory methods")
    class FactoryMethods {

        @Test
        @DisplayName("when creating started result then has processing status and started message")
        void given_started_when_creating_then_has_processing_status() {
            // when
            var result = ImportResult.started(IMPORT_ID);

            // then
            assertThat(result.importId()).isEqualTo(IMPORT_ID);
            assertThat(result.status()).isEqualTo(ImportStatus.PROCESSING);
            assertThat(result.message()).isEqualTo("Import started");
        }

        @Test
        @DisplayName("when creating duplicate result then has completed status and duplicate message")
        void given_duplicate_when_creating_then_has_completed_status() {
            // when
            var result = ImportResult.duplicate(IMPORT_ID);

            // then
            assertThat(result.importId()).isEqualTo(IMPORT_ID);
            assertThat(result.status()).isEqualTo(ImportStatus.COMPLETED);
            assertThat(result.message()).isEqualTo("File already imported");
        }

        @Test
        @DisplayName("when creating in progress result then has processing status and in progress message")
        void given_in_progress_when_creating_then_has_processing_status() {
            // when
            var result = ImportResult.inProgress(IMPORT_ID);

            // then
            assertThat(result.importId()).isEqualTo(IMPORT_ID);
            assertThat(result.status()).isEqualTo(ImportStatus.PROCESSING);
            assertThat(result.message()).isEqualTo("Import already in progress");
        }
    }

    @Nested
    @DisplayName("direct construction")
    class DirectConstruction {

        @Test
        @DisplayName("when creating with all parameters then fields are set correctly")
        void given_all_params_when_creating_then_fields_set() {
            // given
            var importId = "custom-id";
            var status = ImportStatus.FAILED;
            var message = "Custom error message";

            // when
            var result = new ImportResult(importId, status, message);

            // then
            assertThat(result.importId()).isEqualTo(importId);
            assertThat(result.status()).isEqualTo(status);
            assertThat(result.message()).isEqualTo(message);
        }
    }
}
