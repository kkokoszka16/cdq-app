package com.banking.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate root representing a CSV import batch.
 * Tracks import progress, success/error counts, and validation errors.
 */
public final class ImportBatch {

    private final String id;
    private final String filename;
    private final FileChecksum fileChecksum;
    private final LocalDateTime createdAt;

    private ImportStatus status;
    private int totalRows;
    private int successCount;
    private int errorCount;
    private final List<ImportError> errors;
    private LocalDateTime completedAt;

    private ImportBatch(
            String id,
            String filename,
            FileChecksum fileChecksum,
            LocalDateTime createdAt
    ) {
        this.id = validateId(id);
        this.filename = validateFilename(filename);
        this.fileChecksum = validateChecksum(fileChecksum);
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.status = ImportStatus.PENDING;
        this.totalRows = 0;
        this.successCount = 0;
        this.errorCount = 0;
        this.errors = new ArrayList<>();
    }

    public static ImportBatch create(String id, String filename, FileChecksum checksum) {
        return new ImportBatch(id, filename, checksum, LocalDateTime.now());
    }

    public static ImportBatch reconstitute(
            String id,
            String filename,
            FileChecksum checksum,
            ImportStatus status,
            int totalRows,
            int successCount,
            int errorCount,
            List<ImportError> errors,
            LocalDateTime createdAt,
            LocalDateTime completedAt
    ) {
        var batch = new ImportBatch(id, filename, checksum, createdAt);
        batch.status = status;
        batch.totalRows = totalRows;
        batch.successCount = successCount;
        batch.errorCount = errorCount;
        batch.errors.addAll(errors != null ? errors : List.of());
        batch.completedAt = completedAt;
        return batch;
    }

    private String validateId(String batchId) {
        if (batchId == null || batchId.isBlank()) {
            throw new IllegalArgumentException("Batch ID cannot be null or blank");
        }
        return batchId;
    }

    private String validateFilename(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Filename cannot be null or blank");
        }
        return name;
    }

    private FileChecksum validateChecksum(FileChecksum checksum) {
        if (checksum == null) {
            throw new IllegalArgumentException("File checksum cannot be null");
        }
        return checksum;
    }

    public void startProcessing(int rowCount) {
        if (!status.canTransitionTo(ImportStatus.PROCESSING)) {
            throw new IllegalStateException("Cannot start processing from status: " + status);
        }
        this.status = ImportStatus.PROCESSING;
        this.totalRows = rowCount;
    }

    public void recordSuccess() {
        this.successCount++;
    }

    public void recordError(int rowNumber, String message) {
        this.errorCount++;
        this.errors.add(new ImportError(rowNumber, message));
    }

    public void complete() {
        if (!status.canTransitionTo(ImportStatus.COMPLETED)) {
            throw new IllegalStateException("Cannot complete from status: " + status);
        }
        this.status = ImportStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        if (status.isTerminal()) {
            throw new IllegalStateException("Cannot fail from terminal status: " + status);
        }
        this.status = ImportStatus.FAILED;
        this.completedAt = LocalDateTime.now();

        if (reason != null && !reason.isBlank()) {
            this.errors.add(new ImportError(0, reason));
        }
    }

    public String getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public FileChecksum getFileChecksum() {
        return fileChecksum;
    }

    public ImportStatus getStatus() {
        return status;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public List<ImportError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public boolean isCompleted() {
        return status == ImportStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == ImportStatus.FAILED;
    }

    public record ImportError(int rowNumber, String message) {

        public ImportError {
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("Error message cannot be null or blank");
            }
        }
    }
}
