package com.banking.infrastructure.exception;

/**
 * Exception thrown when attempting to import a duplicate file.
 */
public class DuplicateImportException extends RuntimeException {

    private final String existingImportId;

    public DuplicateImportException(String existingImportId, String message) {
        super(message);
        this.existingImportId = existingImportId;
    }

    public String getExistingImportId() {
        return existingImportId;
    }
}
