package com.banking.application.dto;

import com.banking.domain.model.Category;
import com.banking.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * View model representing a single transaction.
 */
public record TransactionView(
        String id,
        String iban,
        LocalDate transactionDate,
        String currency,
        Category category,
        BigDecimal amount,
        String importBatchId
) {

    public static TransactionView from(Transaction transaction) {
        return new TransactionView(
                transaction.id().value(),
                transaction.iban().value(),
                transaction.transactionDate(),
                transaction.currency().getCurrencyCode(),
                transaction.category(),
                transaction.amount().amount(),
                transaction.importBatchId()
        );
    }
}
