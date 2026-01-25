package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.TransactionView;
import com.banking.domain.model.Category;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing a single transaction.
 */
@Schema(description = "Transaction details")
public record TransactionDto(

        @Schema(description = "Unique transaction identifier")
        String id,

        @Schema(description = "International Bank Account Number", example = "PL61109010140000071219812874")
        String iban,

        @Schema(description = "Date of transaction", example = "2024-01-15")
        LocalDate transactionDate,

        @Schema(description = "Currency code (ISO 4217)", example = "PLN")
        String currency,

        @Schema(description = "Transaction category")
        Category category,

        @Schema(description = "Transaction amount (negative for expenses)", example = "-125.50")
        BigDecimal amount,

        @Schema(description = "Import batch identifier")
        String importBatchId
) {

    public static TransactionDto from(TransactionView view) {
        return new TransactionDto(
                view.id(),
                view.iban(),
                view.transactionDate(),
                view.currency(),
                view.category(),
                view.amount(),
                view.importBatchId()
        );
    }
}
