package com.banking.infrastructure.adapter.in.web.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@DisplayName("CsvFileValidator")
@ExtendWith(MockitoExtension.class)
class CsvFileValidatorTest {

    private static final int MAX_FILE_SIZE_MB = 10;
    private static final long BYTES_PER_MB = 1024 * 1024;

    @Mock
    private MultipartFile multipartFile;

    private CsvFileValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CsvFileValidator(MAX_FILE_SIZE_MB);
    }

    @Nested
    @DisplayName("given valid CSV file")
    class GivenValidCsvFile {

        @Test
        @DisplayName("when all conditions met then returns valid result")
        void given_valid_file_when_validate_then_returns_valid() {
            // given
            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getOriginalFilename()).willReturn("transactions.csv");
            given(multipartFile.getContentType()).willReturn("text/csv");
            given(multipartFile.getSize()).willReturn(1024L);

            // when
            var result = validator.validate(multipartFile);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.errorMessage()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"text/csv", "application/octet-stream", "text/plain"})
        @DisplayName("when content type is acceptable then returns valid")
        void given_acceptable_content_type_when_validate_then_valid(String contentType) {
            // given
            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getOriginalFilename()).willReturn("data.csv");
            given(multipartFile.getContentType()).willReturn(contentType);
            given(multipartFile.getSize()).willReturn(500L);

            // when
            var result = validator.validate(multipartFile);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("when content type is null then returns valid")
        void given_null_content_type_when_validate_then_valid() {
            // given
            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getOriginalFilename()).willReturn("data.csv");
            given(multipartFile.getContentType()).willReturn(null);
            given(multipartFile.getSize()).willReturn(500L);

            // when
            var result = validator.validate(multipartFile);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("when uppercase extension then returns valid")
        void given_uppercase_extension_when_validate_then_valid() {
            // given
            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getOriginalFilename()).willReturn("DATA.CSV");
            given(multipartFile.getContentType()).willReturn("text/csv");
            given(multipartFile.getSize()).willReturn(1024L);

            // when
            var result = validator.validate(multipartFile);

            // then
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("given empty file")
    class GivenEmptyFile {

        @Test
        @DisplayName("when file is null then returns invalid")
        void given_null_file_when_validate_then_invalid() {
            // when
            var result = validator.validate(null);

            // then
            assertThat(result.isValid()).isFalse();
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.errorMessage()).isEqualTo("File is empty");
        }

        @Test
        @DisplayName("when file is empty then returns invalid")
        void given_empty_file_when_validate_then_invalid() {
            // given
            given(multipartFile.isEmpty()).willReturn(true);

            // when
            var result = validator.validate(multipartFile);

            // then
            assertThat(result.isValid()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("File is empty");
        }
    }

    @Nested
    @DisplayName("given invalid filename")
    class GivenInvalidFilename {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("when filename is null or blank then returns invalid")
        void given_null_or_blank_filename_when_validate_then_invalid(String filename) {
            // given
            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getOriginalFilename()).willReturn(filename);

            // when
            var result = validator.validate(multipartFile);

            // then
            assertThat(result.isValid()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("Filename is required");
        }

        @ParameterizedTest
        @ValueSource(strings = {"data.txt", "file.xlsx", "transactions.json", "noextension"})
        @DisplayName("when extension is not csv then returns invalid")
        void given_non_csv_extension_when_validate_then_invalid(String filename) {
            // given
            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getOriginalFilename()).willReturn(filename);

            // when
            var result = validator.validate(multipartFile);

            // then
            assertThat(result.isValid()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("File must have .csv extension");
        }
    }

    @Nested
    @DisplayName("given invalid content type")
    class GivenInvalidContentType {

        @ParameterizedTest
        @ValueSource(strings = {"application/json", "image/png", "application/pdf"})
        @DisplayName("when content type is not text or csv then returns invalid")
        void given_invalid_content_type_when_validate_then_invalid(String contentType) {
            // given
            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getOriginalFilename()).willReturn("data.csv");
            given(multipartFile.getContentType()).willReturn(contentType);

            // when
            var result = validator.validate(multipartFile);

            // then
            assertThat(result.isValid()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("Invalid file type. Expected CSV file");
        }
    }

    @Nested
    @DisplayName("given file exceeding size limit")
    class GivenFileSizeExceeded {

        @Test
        @DisplayName("when file too large then returns invalid")
        void given_large_file_when_validate_then_invalid() {
            // given
            var tooLargeSize = (MAX_FILE_SIZE_MB + 1) * BYTES_PER_MB;

            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getOriginalFilename()).willReturn("large.csv");
            given(multipartFile.getContentType()).willReturn("text/csv");
            given(multipartFile.getSize()).willReturn(tooLargeSize);

            // when
            var result = validator.validate(multipartFile);

            // then
            assertThat(result.isValid()).isFalse();
            assertThat(result.errorMessage()).contains("exceeds maximum");
            assertThat(result.errorMessage()).contains(String.valueOf(MAX_FILE_SIZE_MB));
        }

        @Test
        @DisplayName("when file at exact limit then returns valid")
        void given_file_at_limit_when_validate_then_valid() {
            // given
            var exactSize = MAX_FILE_SIZE_MB * BYTES_PER_MB;

            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getOriginalFilename()).willReturn("exact.csv");
            given(multipartFile.getContentType()).willReturn("text/csv");
            given(multipartFile.getSize()).willReturn(exactSize);

            // when
            var result = validator.validate(multipartFile);

            // then
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("given ValidationResult")
    class GivenValidationResult {

        @Test
        @DisplayName("when valid then isValid is true and isInvalid is false")
        void given_valid_result_when_checked_then_state_correct() {
            // given
            var result = CsvFileValidator.ValidationResult.valid();

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.isInvalid()).isFalse();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("when invalid then isValid is false and isInvalid is true")
        void given_invalid_result_when_checked_then_state_correct() {
            // given
            var errorMessage = "Test error";
            var result = CsvFileValidator.ValidationResult.invalid(errorMessage);

            // then
            assertThat(result.isValid()).isFalse();
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.errorMessage()).isEqualTo(errorMessage);
        }
    }
}
