package com.banking.domain.model;

/**
 * Enumeration representing the status of a CSV import batch.
 */
public enum ImportStatus {

    PENDING("Import is queued"),
    PROCESSING("Import is in progress"),
    COMPLETED("Import completed successfully"),
    FAILED("Import failed");

    private final String description;

    ImportStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }

    public boolean canTransitionTo(ImportStatus target) {
        return switch (this) {
            case PENDING -> target == PROCESSING || target == FAILED;
            case PROCESSING -> target == COMPLETED || target == FAILED;
            case COMPLETED, FAILED -> false;
        };
    }
}
