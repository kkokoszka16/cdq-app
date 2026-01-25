package com.banking.application.dto;

import com.banking.domain.model.ImportStatus;

/**
 * Result of import initiation operation.
 */
public record ImportResult(
        String importId,
        ImportStatus status,
        String message
) {

    public static ImportResult started(String importId) {
        return new ImportResult(importId, ImportStatus.PROCESSING, "Import started");
    }

    public static ImportResult duplicate(String existingImportId) {
        return new ImportResult(existingImportId, ImportStatus.COMPLETED, "File already imported");
    }

    public static ImportResult inProgress(String existingImportId) {
        return new ImportResult(existingImportId, ImportStatus.PROCESSING, "Import already in progress");
    }
}
