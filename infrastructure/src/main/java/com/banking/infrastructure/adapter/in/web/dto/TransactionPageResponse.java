package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.TransactionPage;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Paginated response DTO for transactions.
 */
@Schema(description = "Paginated list of transactions")
public record TransactionPageResponse(

        @Schema(description = "List of transactions")
        List<TransactionDto> content,

        @Schema(description = "Current page number (0-indexed)")
        int page,

        @Schema(description = "Page size")
        int size,

        @Schema(description = "Total number of transactions")
        long totalElements,

        @Schema(description = "Total number of pages")
        int totalPages
) {

    public static TransactionPageResponse from(TransactionPage page) {
        var content = page.content().stream()
                .map(TransactionDto::from)
                .toList();

        return new TransactionPageResponse(
                content,
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }
}
