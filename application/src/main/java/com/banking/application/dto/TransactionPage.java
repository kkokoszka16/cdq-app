package com.banking.application.dto;

import java.util.List;

/**
 * Paginated result for transaction queries.
 */
public record TransactionPage(
        List<TransactionView> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static TransactionPage of(List<TransactionView> content, int page, int size, long totalElements) {
        var totalPages = (int) Math.ceil((double) totalElements / size);
        return new TransactionPage(content, page, size, totalElements, totalPages);
    }

    public boolean hasNext() {
        return page < totalPages - 1;
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }
}
