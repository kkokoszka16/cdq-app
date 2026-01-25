package com.banking.domain.model;

import com.banking.domain.exception.InvalidTransactionException;

import java.time.LocalDate;
import java.util.Currency;

/**
 * Core domain entity representing a bank transaction.
 * Immutable record containing all transaction details.
 */
public record Transaction(
        TransactionId id,
        Iban iban,
        LocalDate transactionDate,
        Currency currency,
        Category category,
        Money amount,
        String importBatchId
) {

    private static final int MAX_YEARS_IN_PAST = 10;

    public Transaction {
        validateTransaction(id, iban, transactionDate, currency, category, amount, importBatchId);
    }

    private void validateTransaction(
            TransactionId transactionId,
            Iban transactionIban,
            LocalDate date,
            Currency transactionCurrency,
            Category transactionCategory,
            Money transactionAmount,
            String batchId
    ) {
        if (transactionId == null) {
            throw new InvalidTransactionException("Transaction ID is required");
        }

        if (transactionIban == null) {
            throw new InvalidTransactionException("IBAN is required");
        }

        if (date == null) {
            throw new InvalidTransactionException("Transaction date is required");
        }

        if (date.isAfter(LocalDate.now())) {
            throw new InvalidTransactionException("Transaction date cannot be in the future: " + date);
        }

        var oldestAllowed = LocalDate.now().minusYears(MAX_YEARS_IN_PAST);
        if (date.isBefore(oldestAllowed)) {
            throw new InvalidTransactionException(
                    "Transaction date cannot be older than " + MAX_YEARS_IN_PAST + " years: " + date
            );
        }

        if (transactionCurrency == null) {
            throw new InvalidTransactionException("Currency is required");
        }

        if (transactionCategory == null) {
            throw new InvalidTransactionException("Category is required");
        }

        if (transactionAmount == null) {
            throw new InvalidTransactionException("Amount is required");
        }

        if (batchId == null || batchId.isBlank()) {
            throw new InvalidTransactionException("Import batch ID is required");
        }
    }

    public boolean isIncome() {
        return amount.isPositive();
    }

    public boolean isExpense() {
        return amount.isNegative();
    }

    public int getYear() {
        return transactionDate.getYear();
    }

    public int getMonth() {
        return transactionDate.getMonthValue();
    }
}
