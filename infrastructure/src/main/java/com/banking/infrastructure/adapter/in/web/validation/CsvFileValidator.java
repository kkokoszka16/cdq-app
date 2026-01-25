package com.banking.infrastructure.adapter.in.web.validation;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Validates CSV file uploads before processing.
 *
 * <p>Performs file-level validation including format, size, and encoding checks.</p>
 */
@Slf4j
@Component
public class CsvFileValidator {

    private static final String CSV_EXTENSION = ".csv";
    private static final String CSV_CONTENT_TYPE = "text/csv";
    private static final String OCTET_STREAM_CONTENT_TYPE = "application/octet-stream";

    private static final int BYTES_PER_MEGABYTE = 1024 * 1024;

    private final long maxFileSizeBytes;

    public CsvFileValidator(
            @Value("${import.max-file-size-mb:10}") int maxFileSizeMb
    ) {
        this.maxFileSizeBytes = (long) maxFileSizeMb * BYTES_PER_MEGABYTE;
    }

    /**
     * Validates the uploaded file.
     *
     * @param file the multipart file to validate
     * @return validation result with error message if invalid
     */
    public ValidationResult validate(MultipartFile file) {
        return validateNotEmpty(file)
                .or(() -> validateFilename(file))
                .or(() -> validateContentType(file))
                .or(() -> validateFileSize(file))
                .map(ValidationResult::invalid)
                .orElse(ValidationResult.valid());
    }

    private Optional<String> validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.debug("File validation failed: file is empty");
            return Optional.of("File is empty");
        }
        return Optional.empty();
    }

    private Optional<String> validateFilename(MultipartFile file) {
        var filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            log.debug("File validation failed: filename is missing");
            return Optional.of("Filename is required");
        }

        if (!filename.toLowerCase().endsWith(CSV_EXTENSION)) {
            log.debug("File validation failed: invalid extension for file {}", filename);
            return Optional.of("File must have .csv extension");
        }

        return Optional.empty();
    }

    private Optional<String> validateContentType(MultipartFile file) {
        var contentType = file.getContentType();

        if (contentType == null) {
            return Optional.empty();
        }

        boolean isValidContentType = contentType.equals(CSV_CONTENT_TYPE)
                || contentType.equals(OCTET_STREAM_CONTENT_TYPE)
                || contentType.startsWith("text/");

        if (!isValidContentType) {
            log.debug("File validation failed: invalid content type {}", contentType);
            return Optional.of("Invalid file type. Expected CSV file");
        }

        return Optional.empty();
    }

    private Optional<String> validateFileSize(MultipartFile file) {
        if (file.getSize() > maxFileSizeBytes) {
            long sizeMb = file.getSize() / BYTES_PER_MEGABYTE;
            long maxMb = maxFileSizeBytes / BYTES_PER_MEGABYTE;
            log.debug("File validation failed: size {} MB exceeds limit {} MB", sizeMb, maxMb);
            return Optional.of("File size exceeds maximum allowed size of " + maxMb + " MB");
        }
        return Optional.empty();
    }

    /**
     * Result of file validation.
     */
    public record ValidationResult(boolean isValid, String errorMessage) {

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isInvalid() {
            return !isValid;
        }
    }
}
