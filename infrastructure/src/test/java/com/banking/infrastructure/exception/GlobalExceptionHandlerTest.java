package com.banking.infrastructure.exception;

import com.banking.domain.exception.InvalidAmountException;
import com.banking.domain.exception.InvalidIbanException;
import com.banking.domain.exception.InvalidTransactionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.URI;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("given ResourceNotFoundException")
    class GivenResourceNotFoundException {

        @Test
        @DisplayName("when handled then returns NOT_FOUND status")
        void given_resource_not_found_when_handled_then_returns_not_found_status() {
            // given
            var exception = new ResourceNotFoundException("ImportBatch", "123");

            // when
            var problem = handler.handleResourceNotFound(exception);

            // then
            assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("when handled then returns correct title")
        void given_resource_not_found_when_handled_then_returns_correct_title() {
            // given
            var exception = new ResourceNotFoundException("Transaction", "tx-456");

            // when
            var problem = handler.handleResourceNotFound(exception);

            // then
            assertThat(problem.getTitle()).isEqualTo("Resource Not Found");
        }

        @Test
        @DisplayName("when handled then includes resource details")
        void given_resource_not_found_when_handled_then_includes_resource_details() {
            // given
            var resourceType = "ImportBatch";
            var resourceId = "batch-789";
            var exception = new ResourceNotFoundException(resourceType, resourceId);

            // when
            var problem = handler.handleResourceNotFound(exception);

            // then
            assertThat(problem.getProperties()).containsEntry("resourceType", resourceType);
            assertThat(problem.getProperties()).containsEntry("resourceId", resourceId);
        }

        @Test
        @DisplayName("when handled then includes timestamp")
        void given_resource_not_found_when_handled_then_response_includes_timestamp() {
            // given
            var exception = new ResourceNotFoundException("Test", "1");

            // when
            var problem = handler.handleResourceNotFound(exception);

            // then
            assertThat(problem.getProperties()).containsKey("timestamp");
        }

        @Test
        @DisplayName("when handled then sets type to about:blank")
        void given_resource_not_found_when_handled_then_sets_type_to_about_blank() {
            // given
            var exception = new ResourceNotFoundException("Test", "1");

            // when
            var problem = handler.handleResourceNotFound(exception);

            // then
            assertThat(problem.getType()).isEqualTo(URI.create("about:blank"));
        }
    }

    @Nested
    @DisplayName("given DuplicateImportException")
    class GivenDuplicateImportException {

        @Test
        @DisplayName("when handled then returns CONFLICT status")
        void given_duplicate_import_when_handled_then_returns_conflict_status() {
            // given
            var exception = new DuplicateImportException("existing-id", "File already imported");

            // when
            var problem = handler.handleDuplicateImport(exception);

            // then
            assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        }

        @Test
        @DisplayName("when handled then returns correct title")
        void given_duplicate_import_when_handled_then_returns_correct_title() {
            // given
            var exception = new DuplicateImportException("id", "message");

            // when
            var problem = handler.handleDuplicateImport(exception);

            // then
            assertThat(problem.getTitle()).isEqualTo("Duplicate Import");
        }

        @Test
        @DisplayName("when handled then includes existing import id")
        void given_duplicate_import_when_handled_then_includes_existing_import_id() {
            // given
            var existingId = "import-xyz";
            var exception = new DuplicateImportException(existingId, "message");

            // when
            var problem = handler.handleDuplicateImport(exception);

            // then
            assertThat(problem.getProperties()).containsEntry("existingImportId", existingId);
        }
    }

    @Nested
    @DisplayName("given DomainException")
    class GivenDomainException {

        @ParameterizedTest
        @MethodSource("provideDomainExceptions")
        @DisplayName("when handled then returns BAD_REQUEST status")
        void given_domain_exception_when_handled_then_returns_bad_request_status(Exception exception) {
            // when
            var problem = handler.handleDomainException(
                    (com.banking.domain.exception.DomainException) exception
            );

            // then
            assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("when handled then returns correct title")
        void given_domain_exception_when_handled_then_returns_correct_title() {
            // given
            var exception = new InvalidIbanException("Invalid IBAN");

            // when
            var problem = handler.handleDomainException(exception);

            // then
            assertThat(problem.getTitle()).isEqualTo("Validation Error");
        }

        @Test
        @DisplayName("when handled then includes message in detail")
        void given_domain_exception_when_handled_then_includes_message_in_detail() {
            // given
            var errorMessage = "IBAN validation failed";
            var exception = new InvalidIbanException(errorMessage);

            // when
            var problem = handler.handleDomainException(exception);

            // then
            assertThat(problem.getDetail()).isEqualTo(errorMessage);
        }

        static Stream<Arguments> provideDomainExceptions() {
            return Stream.of(
                    Arguments.of(new InvalidIbanException("Invalid IBAN")),
                    Arguments.of(new InvalidAmountException("Amount must be positive")),
                    Arguments.of(new InvalidTransactionException("Transaction invalid"))
            );
        }
    }

    @Nested
    @DisplayName("given IllegalArgumentException")
    class GivenIllegalArgumentException {

        @Test
        @DisplayName("when handled then returns BAD_REQUEST status")
        void given_illegal_argument_when_handled_then_returns_bad_request_status() {
            // given
            var exception = new IllegalArgumentException("Invalid parameter");

            // when
            var problem = handler.handleIllegalArgument(exception);

            // then
            assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("when handled then returns correct title")
        void given_illegal_argument_when_handled_then_returns_correct_title() {
            // given
            var exception = new IllegalArgumentException("Invalid");

            // when
            var problem = handler.handleIllegalArgument(exception);

            // then
            assertThat(problem.getTitle()).isEqualTo("Invalid Request");
        }
    }

    @Nested
    @DisplayName("given MaxUploadSizeExceededException")
    class GivenMaxUploadSizeExceededException {

        @Test
        @DisplayName("when handled then returns BAD_REQUEST status")
        void given_max_upload_size_when_handled_then_returns_bad_request_status() {
            // given
            var exception = new MaxUploadSizeExceededException(1024L);

            // when
            var problem = handler.handleMaxUploadSize(exception);

            // then
            assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("when handled then returns correct title")
        void given_max_upload_size_when_handled_then_returns_correct_title() {
            // given
            var exception = new MaxUploadSizeExceededException(1024L);

            // when
            var problem = handler.handleMaxUploadSize(exception);

            // then
            assertThat(problem.getTitle()).isEqualTo("File Too Large");
        }

        @Test
        @DisplayName("when handled then returns generic detail message")
        void given_max_upload_size_when_handled_then_returns_generic_detail_message() {
            // given
            var exception = new MaxUploadSizeExceededException(1024L);

            // when
            var problem = handler.handleMaxUploadSize(exception);

            // then
            assertThat(problem.getDetail()).contains("maximum allowed limit");
        }
    }

    @Nested
    @DisplayName("given MissingServletRequestParameterException")
    class GivenMissingServletRequestParameterException {

        @Test
        @DisplayName("when handled then returns BAD_REQUEST status")
        void given_missing_param_when_handled_then_returns_bad_request_status() {
            // given
            var exception = new MissingServletRequestParameterException("importId", "String");

            // when
            var problem = handler.handleMissingParameter(exception);

            // then
            assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("when handled then returns correct title")
        void given_missing_param_when_handled_then_returns_correct_title() {
            // given
            var exception = new MissingServletRequestParameterException("param", "String");

            // when
            var problem = handler.handleMissingParameter(exception);

            // then
            assertThat(problem.getTitle()).isEqualTo("Missing Parameter");
        }

        @Test
        @DisplayName("when handled then includes parameter name in detail")
        void given_missing_param_when_handled_then_includes_parameter_name_in_detail() {
            // given
            var paramName = "accountId";
            var exception = new MissingServletRequestParameterException(paramName, "String");

            // when
            var problem = handler.handleMissingParameter(exception);

            // then
            assertThat(problem.getDetail()).contains(paramName);
            assertThat(problem.getProperties()).containsEntry("parameter", paramName);
        }
    }

    @Nested
    @DisplayName("given MethodArgumentTypeMismatchException")
    class GivenMethodArgumentTypeMismatchException {

        @Test
        @DisplayName("when handled then returns BAD_REQUEST status")
        void given_type_mismatch_when_handled_then_returns_bad_request_status() {
            // given
            var exception = new MethodArgumentTypeMismatchException(
                    "abc",
                    Integer.class,
                    "page",
                    null,
                    new NumberFormatException()
            );

            // when
            var problem = handler.handleTypeMismatch(exception);

            // then
            assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("when handled then returns correct title")
        void given_type_mismatch_when_handled_then_returns_correct_title() {
            // given
            var exception = new MethodArgumentTypeMismatchException(
                    "abc",
                    Integer.class,
                    "size",
                    null,
                    new NumberFormatException()
            );

            // when
            var problem = handler.handleTypeMismatch(exception);

            // then
            assertThat(problem.getTitle()).isEqualTo("Invalid Parameter Type");
        }

        @Test
        @DisplayName("when handled then includes parameter name")
        void given_type_mismatch_when_handled_then_includes_parameter_name() {
            // given
            var paramName = "pageNumber";
            var exception = new MethodArgumentTypeMismatchException(
                    "xyz",
                    Integer.class,
                    paramName,
                    null,
                    new NumberFormatException()
            );

            // when
            var problem = handler.handleTypeMismatch(exception);

            // then
            assertThat(problem.getProperties()).containsEntry("parameter", paramName);
        }
    }

    @Nested
    @DisplayName("given generic Exception")
    class GivenGenericException {

        @Test
        @DisplayName("when handled then returns INTERNAL_SERVER_ERROR status")
        void given_generic_exception_when_handled_then_returns_internal_server_error_status() {
            // given
            var exception = new RuntimeException("Unexpected error");

            // when
            var problem = handler.handleGenericException(exception);

            // then
            assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        @Test
        @DisplayName("when handled then returns correct title")
        void given_generic_exception_when_handled_then_returns_correct_title() {
            // given
            var exception = new RuntimeException("Error");

            // when
            var problem = handler.handleGenericException(exception);

            // then
            assertThat(problem.getTitle()).isEqualTo("Internal Server Error");
        }

        @Test
        @DisplayName("when handled then does not expose exception message")
        void given_generic_exception_when_handled_then_does_not_expose_exception_message() {
            // given
            var sensitiveMessage = "Database connection failed with password xyz";
            var exception = new RuntimeException(sensitiveMessage);

            // when
            var problem = handler.handleGenericException(exception);

            // then
            assertThat(problem.getDetail()).doesNotContain(sensitiveMessage);
            assertThat(problem.getDetail()).isEqualTo("An unexpected error occurred");
        }
    }
}
