package com.banking.infrastructure.adapter.in.web;

import com.banking.application.dto.ImportCommand;
import com.banking.application.dto.TransactionFilter;
import com.banking.application.port.in.GetImportStatusUseCase;
import com.banking.application.port.in.GetTransactionsUseCase;
import com.banking.application.port.in.ImportTransactionsUseCase;
import com.banking.domain.model.Category;
import com.banking.infrastructure.adapter.in.web.dto.ImportResponse;
import com.banking.infrastructure.adapter.in.web.dto.ImportStatusResponse;
import com.banking.infrastructure.adapter.in.web.dto.TransactionPageResponse;
import com.banking.infrastructure.adapter.in.web.validation.CsvFileValidator;
import com.banking.infrastructure.exception.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

/**
 * REST controller for transaction operations.
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction import and query operations")
public class TransactionController {

    private final ImportTransactionsUseCase importTransactionsUseCase;
    private final GetImportStatusUseCase getImportStatusUseCase;
    private final GetTransactionsUseCase getTransactionsUseCase;
    private final CsvFileValidator csvFileValidator;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Import transactions from CSV file")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Import started"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "409", description = "File already imported"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ImportResponse importTransactions(
            @Parameter(description = "CSV file with transactions", required = true)
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        var validationResult = csvFileValidator.validate(file);

        if (validationResult.isInvalid()) {
            throw new IllegalArgumentException(validationResult.errorMessage());
        }

        var command = new ImportCommand(file.getOriginalFilename(), file.getBytes());
        var result = importTransactionsUseCase.importTransactions(command);

        return ImportResponse.from(result);
    }

    @GetMapping("/import/{importId}/status")
    @Operation(summary = "Get import status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status retrieved"),
            @ApiResponse(responseCode = "404", description = "Import not found")
    })
    public ImportStatusResponse getImportStatus(
            @Parameter(description = "Import batch identifier", required = true)
            @PathVariable String importId
    ) {
        return getImportStatusUseCase.getStatus(importId)
                .map(ImportStatusResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch", importId));
    }

    @GetMapping
    @Operation(summary = "List transactions with pagination and filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved")
    })
    public TransactionPageResponse getTransactions(
            @Parameter(description = "Filter by IBAN")
            @RequestParam(value = "iban", required = false) String iban,

            @Parameter(description = "Filter by category")
            @RequestParam(value = "category", required = false) Category category,

            @Parameter(description = "Filter from date (inclusive)")
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Filter to date (inclusive)")
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @Parameter(description = "Page size (max 100)")
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        var filter = new TransactionFilter(iban, category, from, to, page, size);
        var result = getTransactionsUseCase.getTransactions(filter);

        return TransactionPageResponse.from(result);
    }
}
