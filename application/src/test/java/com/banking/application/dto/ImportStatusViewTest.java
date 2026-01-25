package com.banking.application.dto;

import com.banking.domain.model.FileChecksum;
import com.banking.domain.model.ImportBatch;
import com.banking.domain.model.ImportStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ImportStatusView")
class ImportStatusViewTest {

    private static final String BATCH_ID = "batch-001";
    private static final String FILENAME = "transactions.csv";
    private static final FileChecksum CHECKSUM = FileChecksum.of("test content");

    @Nested
    @DisplayName("from batch")
    class FromBatch {

        @Test
        @DisplayName("when converting pending batch then all fields are mapped")
        void given_pending_batch_when_converting_then_fields_mapped() {
            // given
            var batch = ImportBatch.create(BATCH_ID, FILENAME, CHECKSUM);

            // when
            var view = ImportStatusView.from(batch);

            // then
            assertThat(view.importId()).isEqualTo(BATCH_ID);
            assertThat(view.status()).isEqualTo(ImportStatus.PENDING);
            assertThat(view.filename()).isEqualTo(FILENAME);
            assertThat(view.totalRows()).isZero();
            assertThat(view.successCount()).isZero();
            assertThat(view.errorCount()).isZero();
            assertThat(view.errors()).isEmpty();
            assertThat(view.createdAt()).isNotNull();
            assertThat(view.completedAt()).isNull();
        }

        @Test
        @DisplayName("when converting processing batch then status is processing")
        void given_processing_batch_when_converting_then_status_processing() {
            // given
            var batch = ImportBatch.create(BATCH_ID, FILENAME, CHECKSUM);
            batch.startProcessing(100);

            // when
            var view = ImportStatusView.from(batch);

            // then
            assertThat(view.status()).isEqualTo(ImportStatus.PROCESSING);
            assertThat(view.totalRows()).isEqualTo(100);
        }

        @Test
        @DisplayName("when converting completed batch then has completedAt")
        void given_completed_batch_when_converting_then_has_completed_at() {
            // given
            var batch = ImportBatch.create(BATCH_ID, FILENAME, CHECKSUM);
            batch.startProcessing(100);
            batch.recordSuccess();
            batch.recordSuccess();
            batch.complete();

            // when
            var view = ImportStatusView.from(batch);

            // then
            assertThat(view.status()).isEqualTo(ImportStatus.COMPLETED);
            assertThat(view.successCount()).isEqualTo(2);
            assertThat(view.completedAt()).isNotNull();
        }

        @Test
        @DisplayName("when converting batch with errors then errors are mapped")
        void given_batch_with_errors_when_converting_then_errors_mapped() {
            // given
            var batch = ImportBatch.create(BATCH_ID, FILENAME, CHECKSUM);
            batch.startProcessing(5);
            batch.recordSuccess();
            batch.recordError(2, "Invalid IBAN");
            batch.recordError(4, "Invalid amount");
            batch.recordSuccess();
            batch.complete();

            // when
            var view = ImportStatusView.from(batch);

            // then
            assertThat(view.successCount()).isEqualTo(2);
            assertThat(view.errorCount()).isEqualTo(2);
            assertThat(view.errors()).hasSize(2);
            assertThat(view.errors().get(0).row()).isEqualTo(2);
            assertThat(view.errors().get(0).message()).isEqualTo("Invalid IBAN");
            assertThat(view.errors().get(1).row()).isEqualTo(4);
            assertThat(view.errors().get(1).message()).isEqualTo("Invalid amount");
        }

        @Test
        @DisplayName("when converting failed batch then status is failed")
        void given_failed_batch_when_converting_then_status_failed() {
            // given
            var batch = ImportBatch.create(BATCH_ID, FILENAME, CHECKSUM);
            batch.fail("Connection timeout");

            // when
            var view = ImportStatusView.from(batch);

            // then
            assertThat(view.status()).isEqualTo(ImportStatus.FAILED);
            assertThat(view.completedAt()).isNotNull();
            assertThat(view.errors()).hasSize(1);
            assertThat(view.errors().getFirst().message()).isEqualTo("Connection timeout");
        }
    }

    @Nested
    @DisplayName("ErrorDetail")
    class ErrorDetailTest {

        @Test
        @DisplayName("when creating error detail then fields are set correctly")
        void given_valid_params_when_creating_then_fields_set() {
            // when
            var errorDetail = new ImportStatusView.ErrorDetail(10, "Invalid format");

            // then
            assertThat(errorDetail.row()).isEqualTo(10);
            assertThat(errorDetail.message()).isEqualTo("Invalid format");
        }
    }
}
