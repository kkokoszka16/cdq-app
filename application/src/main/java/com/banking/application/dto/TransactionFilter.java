package com.banking.application.dto;

import com.banking.domain.model.Category;

import java.time.LocalDate;

/**
 * Filter criteria for transaction queries.
 */
public record TransactionFilter(
        String iban,
        Category category,
        LocalDate from,
        LocalDate to,
        int page,
        int size
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public TransactionFilter {
        page = Math.max(page, DEFAULT_PAGE);
        size = Math.min(Math.max(size, 1), MAX_SIZE);
    }

    public static TransactionFilter defaults() {
        return new TransactionFilter(null, null, null, null, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public TransactionFilter withIban(String newIban) {
        return new TransactionFilter(newIban, category, from, to, page, size);
    }

    public TransactionFilter withCategory(Category newCategory) {
        return new TransactionFilter(iban, newCategory, from, to, page, size);
    }

    public TransactionFilter withDateRange(LocalDate newFrom, LocalDate newTo) {
        return new TransactionFilter(iban, category, newFrom, newTo, page, size);
    }

    public TransactionFilter withPagination(int newPage, int newSize) {
        return new TransactionFilter(iban, category, from, to, newPage, newSize);
    }
}
