package com.banking.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ImportBatch")
class ImportBatchTest {

    private static final String VALID_ID = "batch-001";
    private static final String VALID_FILENAME = "transactions.csv";
    private static final FileChecksum VALID_CHECKSUM = FileChecksum.of("test content");

    @Nested
    @DisplayName("given valid parameters")
    class GivenValidParameters {

        @Test
        @DisplayName("when creating batch then all fields are initialized correctly")
        void given_valid_params_when_creating_then_fields_initialized() {
            // when
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);

            // then
            assertThat(batch.getId()).isEqualTo(VALID_ID);
            assertThat(batch.getFilename()).isEqualTo(VALID_FILENAME);
            assertThat(batch.getFileChecksum()).isEqualTo(VALID_CHECKSUM);
            assertThat(batch.getStatus()).isEqualTo(ImportStatus.PENDING);
            assertThat(batch.getTotalRows()).isZero();
            assertThat(batch.getSuccessCount()).isZero();
            assertThat(batch.getErrorCount()).isZero();
            assertThat(batch.getErrors()).isEmpty();
            assertThat(batch.getCreatedAt()).isNotNull();
            assertThat(batch.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("when creating batch then isCompleted is false")
        void given_new_batch_when_checking_completed_then_returns_false() {
            // when
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);

            // then
            assertThat(batch.isCompleted()).isFalse();
            assertThat(batch.isFailed()).isFalse();
        }
    }

    @Nested
    @DisplayName("given invalid parameters")
    class GivenInvalidParameters {

        @ParameterizedTest(name = "id: [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("when id is null/empty/blank then throws exception")
        void given_invalid_id_when_creating_then_throws_exception(String id) {
            // when/then
            assertThatThrownBy(() -> ImportBatch.create(id, VALID_FILENAME, VALID_CHECKSUM))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Batch ID cannot be null or blank");
        }

        @ParameterizedTest(name = "filename: [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("when filename is null/empty/blank then throws exception")
        void given_invalid_filename_when_creating_then_throws_exception(String filename) {
            // when/then
            assertThatThrownBy(() -> ImportBatch.create(VALID_ID, filename, VALID_CHECKSUM))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Filename cannot be null or blank");
        }

        @Test
        @DisplayName("when checksum is null then throws exception")
        void given_null_checksum_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> ImportBatch.create(VALID_ID, VALID_FILENAME, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("File checksum cannot be null");
        }
    }

    @Nested
    @DisplayName("state transitions")
    class StateTransitions {

        @Test
        @DisplayName("when starting processing from pending then transitions to processing")
        void given_pending_batch_when_starting_processing_then_transitions() {
            // given
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);
            var rowCount = 100;

            // when
            batch.startProcessing(rowCount);

            // then
            assertThat(batch.getStatus()).isEqualTo(ImportStatus.PROCESSING);
            assertThat(batch.getTotalRows()).isEqualTo(rowCount);
        }

        @Test
        @DisplayName("when starting processing from non-pending then throws exception")
        void given_processing_batch_when_starting_processing_again_then_throws_exception() {
            // given
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);
            batch.startProcessing(100);

            // when/then
            assertThatThrownBy(() -> batch.startProcessing(50))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot start processing from status");
        }

        @Test
        @DisplayName("when completing from processing then transitions to completed")
        void given_processing_batch_when_completing_then_transitions() {
            // given
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);
            batch.startProcessing(100);

            // when
            batch.complete();

            // then
            assertThat(batch.getStatus()).isEqualTo(ImportStatus.COMPLETED);
            assertThat(batch.isCompleted()).isTrue();
            assertThat(batch.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("when completing from pending then throws exception")
        void given_pending_batch_when_completing_then_throws_exception() {
            // given
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);

            // when/then
            assertThatThrownBy(batch::complete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot complete from status");
        }

        @Test
        @DisplayName("when failing from pending then transitions to failed")
        void given_pending_batch_when_failing_then_transitions() {
            // given
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);
            var reason = "Connection timeout";

            // when
            batch.fail(reason);

            // then
            assertThat(batch.getStatus()).isEqualTo(ImportStatus.FAILED);
            assertThat(batch.isFailed()).isTrue();
            assertThat(batch.getCompletedAt()).isNotNull();
            assertThat(batch.getErrors()).hasSize(1);
            assertThat(batch.getErrors().getFirst().message()).isEqualTo(reason);
        }

        @Test
        @DisplayName("when failing from processing then transitions to failed")
        void given_processing_batch_when_failing_then_transitions() {
            // given
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);
            batch.startProcessing(100);
            var reason = "Database error";

            // when
            batch.fail(reason);

            // then
            assertThat(batch.getStatus()).isEqualTo(ImportStatus.FAILED);
            assertThat(batch.isFailed()).isTrue();
        }

        @Test
        @DisplayName("when failing from completed then throws exception")
        void given_completed_batch_when_failing_then_throws_exception() {
            // given
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);
            batch.startProcessing(100);
            batch.complete();

            // when/then
            assertThatThrownBy(() -> batch.fail("reason"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot fail from terminal status");
        }

        @Test
        @DisplayName("when failing from failed then throws exception")
        void given_failed_batch_when_failing_then_throws_exception() {
            // given
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);
            batch.fail("first failure");

            // when/then
            assertThatThrownBy(() -> batch.fail("second failure"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot fail from terminal status");
        }

        @Test
        @DisplayName("when failing with null reason then no error added")
        void given_null_reason_when_failing_then_no_error_added() {
            // given
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);

            // when
            batch.fail(null);

            // then
            assertThat(batch.isFailed()).isTrue();
            assertThat(batch.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("when failing with blank reason then no error added")
        void given_blank_reason_when_failing_then_no_error_added() {
            // given
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);

            // when
            batch.fail("   ");

            // then
            assertThat(batch.isFailed()).isTrue();
            assertThat(batch.getErrors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("recording results")
    class RecordingResults {

        @Test
        @DisplayName("when recording success then increments success count")
        void given_batch_when_recording_success_then_increments_count() {
            // given
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);
            batch.startProcessing(10);

            // when
            batch.recordSuccess();
            batch.recordSuccess();
            batch.recordSuccess();

            // then
            assertThat(batch.getSuccessCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("when recording error then increments error count and adds error")
        void given_batch_when_recording_error_then_increments_and_adds() {
            // given
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);
            batch.startProcessing(10);

            // when
            batch.recordError(5, "Invalid IBAN");
            batch.recordError(8, "Invalid amount");

            // then
            assertThat(batch.getErrorCount()).isEqualTo(2);
            assertThat(batch.getErrors()).hasSize(2);
            assertThat(batch.getErrors().get(0).rowNumber()).isEqualTo(5);
            assertThat(batch.getErrors().get(0).message()).isEqualTo("Invalid IBAN");
            assertThat(batch.getErrors().get(1).rowNumber()).isEqualTo(8);
            assertThat(batch.getErrors().get(1).message()).isEqualTo("Invalid amount");
        }

        @Test
        @DisplayName("when getting errors then returns unmodifiable list")
        void given_batch_with_errors_when_getting_errors_then_returns_unmodifiable() {
            // given
            var batch = ImportBatch.create(VALID_ID, VALID_FILENAME, VALID_CHECKSUM);
            batch.startProcessing(10);
            batch.recordError(1, "Error 1");

            // when
            var errors = batch.getErrors();

            // then
            assertThatThrownBy(() -> errors.add(new ImportBatch.ImportError(2, "Error 2")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("reconstitution")
    class Reconstitution {

        @Test
        @DisplayName("when reconstituting batch then all fields are restored")
        void given_stored_data_when_reconstituting_then_fields_restored() {
            // given
            var id = "reconstituted-id";
            var filename = "restored.csv";
            var checksum = FileChecksum.of("stored content");
            var status = ImportStatus.COMPLETED;
            var totalRows = 500;
            var successCount = 480;
            var errorCount = 20;
            var errors = List.of(
                    new ImportBatch.ImportError(10, "Error at row 10"),
                    new ImportBatch.ImportError(25, "Error at row 25")
            );
            var createdAt = LocalDateTime.of(2024, 1, 15, 10, 30);
            var completedAt = LocalDateTime.of(2024, 1, 15, 10, 45);

            // when
            var batch = ImportBatch.reconstitute(
                    id, filename, checksum, status,
                    totalRows, successCount, errorCount, errors,
                    createdAt, completedAt
            );

            // then
            assertThat(batch.getId()).isEqualTo(id);
            assertThat(batch.getFilename()).isEqualTo(filename);
            assertThat(batch.getFileChecksum()).isEqualTo(checksum);
            assertThat(batch.getStatus()).isEqualTo(status);
            assertThat(batch.getTotalRows()).isEqualTo(totalRows);
            assertThat(batch.getSuccessCount()).isEqualTo(successCount);
            assertThat(batch.getErrorCount()).isEqualTo(errorCount);
            assertThat(batch.getErrors()).hasSize(2);
            assertThat(batch.getCreatedAt()).isEqualTo(createdAt);
            assertThat(batch.getCompletedAt()).isEqualTo(completedAt);
        }

        @Test
        @DisplayName("when reconstituting with null errors then uses empty list")
        void given_null_errors_when_reconstituting_then_uses_empty_list() {
            // when
            var batch = ImportBatch.reconstitute(
                    VALID_ID, VALID_FILENAME, VALID_CHECKSUM, ImportStatus.COMPLETED,
                    100, 100, 0, null,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            // then
            assertThat(batch.getErrors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ImportError")
    class ImportErrorTest {

        @Test
        @DisplayName("when creating error with valid data then fields are set")
        void given_valid_data_when_creating_error_then_fields_set() {
            // when
            var error = new ImportBatch.ImportError(10, "Invalid IBAN format");

            // then
            assertThat(error.rowNumber()).isEqualTo(10);
            assertThat(error.message()).isEqualTo("Invalid IBAN format");
        }

        @ParameterizedTest(name = "message: [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("when creating error with null/empty/blank message then throws exception")
        void given_invalid_message_when_creating_error_then_throws_exception(String message) {
            // when/then
            assertThatThrownBy(() -> new ImportBatch.ImportError(10, message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Error message cannot be null or blank");
        }

        @Test
        @DisplayName("when creating error with zero row number then succeeds")
        void given_zero_row_number_when_creating_error_then_succeeds() {
            // when
            var error = new ImportBatch.ImportError(0, "General error");

            // then
            assertThat(error.rowNumber()).isZero();
        }
    }
}
