package com.banking.application.dto;

import com.banking.domain.model.ImportBatch;
import com.banking.domain.model.ImportStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * View model representing import batch status.
 */
public record ImportStatusView(
        String importId,
        ImportStatus status,
        String filename,
        int totalRows,
        int successCount,
        int errorCount,
        List<ErrorDetail> errors,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {

    public static ImportStatusView from(ImportBatch batch) {
        var errorDetails = batch.getErrors().stream()
                .map(error -> new ErrorDetail(error.rowNumber(), error.message()))
                .toList();

        return new ImportStatusView(
                batch.getId(),
                batch.getStatus(),
                batch.getFilename(),
                batch.getTotalRows(),
                batch.getSuccessCount(),
                batch.getErrorCount(),
                errorDetails,
                batch.getCreatedAt(),
                batch.getCompletedAt()
        );
    }

    public record ErrorDetail(int row, String message) {
    }
}
