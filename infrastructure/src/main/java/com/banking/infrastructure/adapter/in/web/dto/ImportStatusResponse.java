package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.ImportStatusView;
import com.banking.domain.model.ImportStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for import status query.
 */
@Schema(description = "Import batch status details")
public record ImportStatusResponse(

        @Schema(description = "Unique import batch identifier")
        String importId,

        @Schema(description = "Current import status")
        ImportStatus status,

        @Schema(description = "Original filename")
        String filename,

        @Schema(description = "Total rows processed")
        int totalRows,

        @Schema(description = "Successfully imported rows")
        int successCount,

        @Schema(description = "Rows with errors")
        int errorCount,

        @Schema(description = "List of validation errors")
        List<ErrorDetail> errors,

        @Schema(description = "Import creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Import completion timestamp")
        LocalDateTime completedAt
) {

    public static ImportStatusResponse from(ImportStatusView view) {
        var errorDetails = view.errors().stream()
                .map(error -> new ErrorDetail(error.row(), error.message()))
                .toList();

        return new ImportStatusResponse(
                view.importId(),
                view.status(),
                view.filename(),
                view.totalRows(),
                view.successCount(),
                view.errorCount(),
                errorDetails,
                view.createdAt(),
                view.completedAt()
        );
    }

    @Schema(description = "Validation error detail")
    public record ErrorDetail(

            @Schema(description = "Row number with error")
            int row,

            @Schema(description = "Error message")
            String message
    ) {
    }
}
