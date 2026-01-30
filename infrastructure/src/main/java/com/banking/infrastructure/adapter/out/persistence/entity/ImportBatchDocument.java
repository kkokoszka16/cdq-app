package com.banking.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document representing an import batch.
 */
@Document(collection = "import_batches")
@CompoundIndexes({
        @CompoundIndex(name = "checksum_status_idx", def = "{'fileChecksum': 1, 'status': 1}"),
        @CompoundIndex(name = "created_at_idx", def = "{'createdAt': -1}")
})
public class ImportBatchDocument {

    @Id
    private String id;
    private String filename;
    private String fileChecksum;
    private String status;
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<ImportErrorDocument> errors;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public ImportBatchDocument() {
        this.errors = new ArrayList<>();
    }

    public ImportBatchDocument(
            String id,
            String filename,
            String fileChecksum,
            String status,
            int totalRows,
            int successCount,
            int errorCount,
            List<ImportErrorDocument> errors,
            LocalDateTime createdAt,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.filename = filename;
        this.fileChecksum = fileChecksum;
        this.status = status;
        this.totalRows = totalRows;
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileChecksum() {
        return fileChecksum;
    }

    public void setFileChecksum(String fileChecksum) {
        this.fileChecksum = fileChecksum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<ImportErrorDocument> getErrors() {
        return errors;
    }

    public void setErrors(List<ImportErrorDocument> errors) {
        this.errors = errors;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public static class ImportErrorDocument {

        private int rowNumber;
        private String message;

        public ImportErrorDocument() {
        }

        public ImportErrorDocument(int rowNumber, String message) {
            this.rowNumber = rowNumber;
            this.message = message;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
