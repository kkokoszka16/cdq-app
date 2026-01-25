package com.banking.application.dto;

/**
 * Command object for initiating CSV file import.
 */
public record ImportCommand(
        String filename,
        byte[] content
) {

    public ImportCommand {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename cannot be null or blank");
        }

        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
    }
}
