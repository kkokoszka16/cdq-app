package com.banking.application.dto;

import java.util.List;

/**
 * Result of CSV parsing operation containing valid transactions and errors.
 */
public record CsvParseResult(
        List<ParsedTransaction> validTransactions,
        List<ParseError> errors,
        int totalRowsProcessed
) {

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public int successCount() {
        return validTransactions.size();
    }

    public int errorCount() {
        return errors.size();
    }

    public record ParseError(int rowNumber, String message) {
    }
}
