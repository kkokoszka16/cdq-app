package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.ImportStatusView;
import com.banking.application.dto.ImportStatusView.ErrorDetail;
import com.banking.domain.model.ImportStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ImportStatusResponse DTO.
 */
@DisplayName("ImportStatusResponse")
class ImportStatusResponseTest {

    private static final String VALID_IMPORT_ID = "import-status-test-123";
    private static final String VALID_FILENAME = "transactions.csv";
    private static final LocalDateTime VALID_CREATED_AT = LocalDateTime.of(2024, 6, 15, 10, 0, 0);
    private static final LocalDateTime VALID_COMPLETED_AT = LocalDateTime.of(2024, 6, 15, 10, 5, 0);

    @Nested
    @DisplayName("given record constructor")
    class GivenRecordConstructor {

        @Test
        @DisplayName("when created with valid values then all fields are set")
        void given_valid_values_when_created_then_all_fields_set() {
            // given
            var errors = List.of(
                    new ImportStatusResponse.ErrorDetail(5, "Invalid IBAN")
            );

            // when
            var response = new ImportStatusResponse(
                    VALID_IMPORT_ID,
                    ImportStatus.COMPLETED,
                    VALID_FILENAME,
                    100,
                    99,
                    1,
                    errors,
                    VALID_CREATED_AT,
                    VALID_COMPLETED_AT
            );

            // then
            assertThat(response.importId()).isEqualTo(VALID_IMPORT_ID);
            assertThat(response.status()).isEqualTo(ImportStatus.COMPLETED);
            assertThat(response.filename()).isEqualTo(VALID_FILENAME);
            assertThat(response.totalRows()).isEqualTo(100);
            assertThat(response.successCount()).isEqualTo(99);
            assertThat(response.errorCount()).isEqualTo(1);
            assertThat(response.errors()).hasSize(1);
            assertThat(response.createdAt()).isEqualTo(VALID_CREATED_AT);
            assertThat(response.completedAt()).isEqualTo(VALID_COMPLETED_AT);
        }

        @Test
        @DisplayName("when created with empty errors then errors list is empty")
        void given_empty_errors_when_created_then_errors_empty() {
            // when
            var response = new ImportStatusResponse(
                    VALID_IMPORT_ID,
                    ImportStatus.COMPLETED,
                    VALID_FILENAME,
                    100,
                    100,
                    0,
                    List.of(),
                    VALID_CREATED_AT,
                    VALID_COMPLETED_AT
            );

            // then
            assertThat(response.errors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("given from factory method")
    class GivenFromFactoryMethod {

        @Test
        @DisplayName("when converting from ImportStatusView then maps all fields")
        void given_import_status_view_when_from_then_maps_all_fields() {
            // given
            var errors = List.of(
                    new ErrorDetail(3, "Missing amount"),
                    new ErrorDetail(7, "Invalid date")
            );

            var view = new ImportStatusView(
                    VALID_IMPORT_ID,
                    ImportStatus.COMPLETED,
                    VALID_FILENAME,
                    200,
                    198,
                    2,
                    errors,
                    VALID_CREATED_AT,
                    VALID_COMPLETED_AT
            );

            // when
            var response = ImportStatusResponse.from(view);

            // then
            assertThat(response.importId()).isEqualTo(VALID_IMPORT_ID);
            assertThat(response.status()).isEqualTo(ImportStatus.COMPLETED);
            assertThat(response.filename()).isEqualTo(VALID_FILENAME);
            assertThat(response.totalRows()).isEqualTo(200);
            assertThat(response.successCount()).isEqualTo(198);
            assertThat(response.errorCount()).isEqualTo(2);
            assertThat(response.errors()).hasSize(2);
            assertThat(response.createdAt()).isEqualTo(VALID_CREATED_AT);
            assertThat(response.completedAt()).isEqualTo(VALID_COMPLETED_AT);
        }

        @Test
        @DisplayName("when converting view with errors then maps error details")
        void given_view_with_errors_when_from_then_maps_error_details() {
            // given
            var errors = List.of(
                    new ErrorDetail(1, "First error"),
                    new ErrorDetail(99, "Last error")
            );

            var view = new ImportStatusView(
                    VALID_IMPORT_ID,
                    ImportStatus.FAILED,
                    VALID_FILENAME,
                    100,
                    98,
                    2,
                    errors,
                    VALID_CREATED_AT,
                    VALID_COMPLETED_AT
            );

            // when
            var response = ImportStatusResponse.from(view);

            // then
            assertThat(response.errors()).hasSize(2);
            assertThat(response.errors().get(0).row()).isEqualTo(1);
            assertThat(response.errors().get(0).message()).isEqualTo("First error");
            assertThat(response.errors().get(1).row()).isEqualTo(99);
            assertThat(response.errors().get(1).message()).isEqualTo("Last error");
        }

        @ParameterizedTest
        @EnumSource(ImportStatus.class)
        @DisplayName("when converting view with any status then maps status")
        void given_any_status_when_from_then_maps_status(ImportStatus status) {
            // given
            var view = new ImportStatusView(
                    VALID_IMPORT_ID,
                    status,
                    VALID_FILENAME,
                    100,
                    100,
                    0,
                    List.of(),
                    VALID_CREATED_AT,
                    status.isTerminal() ? VALID_COMPLETED_AT : null
            );

            // when
            var response = ImportStatusResponse.from(view);

            // then
            assertThat(response.status()).isEqualTo(status);
        }

        @Test
        @DisplayName("when converting pending view then completedAt is null")
        void given_pending_view_when_from_then_completed_at_null() {
            // given
            var view = new ImportStatusView(
                    VALID_IMPORT_ID,
                    ImportStatus.PENDING,
                    VALID_FILENAME,
                    0,
                    0,
                    0,
                    List.of(),
                    VALID_CREATED_AT,
                    null
            );

            // when
            var response = ImportStatusResponse.from(view);

            // then
            assertThat(response.completedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("given ErrorDetail nested record")
    class GivenErrorDetailNestedRecord {

        @Test
        @DisplayName("when created with valid values then all fields are set")
        void given_valid_values_when_created_then_all_fields_set() {
            // when
            var errorDetail = new ImportStatusResponse.ErrorDetail(42, "Invalid format");

            // then
            assertThat(errorDetail.row()).isEqualTo(42);
            assertThat(errorDetail.message()).isEqualTo("Invalid format");
        }

        @Test
        @DisplayName("when created with zero row then stores zero")
        void given_zero_row_when_created_then_stores_zero() {
            // when
            var errorDetail = new ImportStatusResponse.ErrorDetail(0, "Global error");

            // then
            assertThat(errorDetail.row()).isZero();
        }
    }
}
