package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.ImportResult;
import com.banking.domain.model.ImportStatus;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for import operation.
 */
@Schema(description = "Import operation response")
public record ImportResponse(

        @Schema(description = "Unique import batch identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        String importId,

        @Schema(description = "Current import status", example = "PROCESSING")
        ImportStatus status,

        @Schema(description = "Status message", example = "Import started")
        String message
) {

    public static ImportResponse from(ImportResult result) {
        return new ImportResponse(result.importId(), result.status(), result.message());
    }
}
