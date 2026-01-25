package com.banking.infrastructure.adapter.in.web;

import com.banking.application.dto.ImportResult;
import com.banking.application.dto.ImportStatusView;
import com.banking.application.dto.TransactionPage;
import com.banking.application.dto.TransactionView;
import com.banking.application.port.in.GetImportStatusUseCase;
import com.banking.application.port.in.GetTransactionsUseCase;
import com.banking.application.port.in.ImportTransactionsUseCase;
import com.banking.domain.model.Category;
import com.banking.domain.model.ImportStatus;
import com.banking.infrastructure.adapter.in.web.validation.CsvFileValidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@DisplayName("TransactionController")
class TransactionControllerTest {

    private static final String IMPORT_ENDPOINT = "/api/v1/transactions/import";
    private static final String STATUS_ENDPOINT = "/api/v1/transactions/import/{importId}/status";
    private static final String LIST_ENDPOINT = "/api/v1/transactions";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImportTransactionsUseCase importTransactionsUseCase;

    @MockBean
    private GetImportStatusUseCase getImportStatusUseCase;

    @MockBean
    private GetTransactionsUseCase getTransactionsUseCase;

    @MockBean
    private CsvFileValidator csvFileValidator;

    @Nested
    @DisplayName("POST /import")
    class ImportEndpoint {

        @Test
        @WithMockUser
        @DisplayName("given valid CSV file when importing then returns 202 Accepted")
        void given_valid_csv_file_when_importing_then_returns_accepted() throws Exception {
            // given
            var file = new MockMultipartFile(
                    "file",
                    "transactions.csv",
                    "text/csv",
                    "iban,date,currency,category,amount\nPL61109010140000071219812874,2024-01-15,PLN,FOOD,-100.00".getBytes()
            );
            var result = ImportResult.started("batch-123");
            given(csvFileValidator.validate(any())).willReturn(CsvFileValidator.ValidationResult.valid());
            given(importTransactionsUseCase.importTransactions(any())).willReturn(result);

            // when/then
            mockMvc.perform(multipart(IMPORT_ENDPOINT)
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.importId").value("batch-123"))
                    .andExpect(jsonPath("$.status").value("PROCESSING"))
                    .andExpect(jsonPath("$.message").value("Import started"));
        }

        @Test
        @WithMockUser
        @DisplayName("given empty file when importing then returns 400 Bad Request")
        void given_empty_file_when_importing_then_returns_bad_request() throws Exception {
            // given
            var file = new MockMultipartFile(
                    "file",
                    "empty.csv",
                    "text/csv",
                    new byte[0]
            );
            given(csvFileValidator.validate(any())).willReturn(
                    CsvFileValidator.ValidationResult.invalid("File is empty")
            );

            // when/then
            mockMvc.perform(multipart(IMPORT_ENDPOINT)
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("given non-CSV file when importing then returns 400 Bad Request")
        void given_non_csv_file_when_importing_then_returns_bad_request() throws Exception {
            // given
            var file = new MockMultipartFile(
                    "file",
                    "data.txt",
                    "text/plain",
                    "some content".getBytes()
            );
            given(csvFileValidator.validate(any())).willReturn(
                    CsvFileValidator.ValidationResult.invalid("File must have .csv extension")
            );

            // when/then
            mockMvc.perform(multipart(IMPORT_ENDPOINT)
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("given duplicate file when importing then returns duplicate result")
        void given_duplicate_file_when_importing_then_returns_duplicate() throws Exception {
            // given
            var file = new MockMultipartFile(
                    "file",
                    "transactions.csv",
                    "text/csv",
                    "content".getBytes()
            );
            var result = ImportResult.duplicate("existing-batch-id");
            given(csvFileValidator.validate(any())).willReturn(CsvFileValidator.ValidationResult.valid());
            given(importTransactionsUseCase.importTransactions(any())).willReturn(result);

            // when/then
            mockMvc.perform(multipart(IMPORT_ENDPOINT)
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.importId").value("existing-batch-id"))
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.message").value("File already imported"));
        }
    }

    @Nested
    @DisplayName("GET /import/{importId}/status")
    class StatusEndpoint {

        @Test
        @WithMockUser
        @DisplayName("given existing batch when getting status then returns status")
        void given_existing_batch_when_getting_status_then_returns_status() throws Exception {
            // given
            var importId = "batch-123";
            var statusView = new ImportStatusView(
                    importId,
                    ImportStatus.COMPLETED,
                    "transactions.csv",
                    100,
                    98,
                    2,
                    List.of(
                            new ImportStatusView.ErrorDetail(5, "Invalid IBAN"),
                            new ImportStatusView.ErrorDetail(12, "Invalid amount")
                    ),
                    LocalDateTime.of(2024, 1, 15, 10, 30, 0),
                    LocalDateTime.of(2024, 1, 15, 10, 30, 5)
            );
            given(getImportStatusUseCase.getStatus(importId)).willReturn(Optional.of(statusView));

            // when/then
            mockMvc.perform(get(STATUS_ENDPOINT, importId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.importId").value(importId))
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.filename").value("transactions.csv"))
                    .andExpect(jsonPath("$.totalRows").value(100))
                    .andExpect(jsonPath("$.successCount").value(98))
                    .andExpect(jsonPath("$.errorCount").value(2))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[0].row").value(5))
                    .andExpect(jsonPath("$.errors[0].message").value("Invalid IBAN"));
        }

        @Test
        @WithMockUser
        @DisplayName("given non-existing batch when getting status then returns 404")
        void given_non_existing_batch_when_getting_status_then_returns_not_found() throws Exception {
            // given
            var importId = "non-existing-id";
            given(getImportStatusUseCase.getStatus(importId)).willReturn(Optional.empty());

            // when/then
            mockMvc.perform(get(STATUS_ENDPOINT, importId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /transactions")
    class ListEndpoint {

        @Test
        @WithMockUser
        @DisplayName("given transactions exist when listing then returns paginated results")
        void given_transactions_when_listing_then_returns_paginated() throws Exception {
            // given
            var transactions = List.of(
                    new TransactionView(
                            "tx-1",
                            "PL61109010140000071219812874",
                            LocalDate.of(2024, 1, 15),
                            "PLN",
                            Category.FOOD,
                            new BigDecimal("-100.00"),
                            "batch-1"
                    )
            );
            var page = new TransactionPage(transactions, 0, 20, 1, 1);
            given(getTransactionsUseCase.getTransactions(any())).willReturn(page);

            // when/then
            mockMvc.perform(get(LIST_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value("tx-1"))
                    .andExpect(jsonPath("$.content[0].iban").value("PL61109010140000071219812874"))
                    .andExpect(jsonPath("$.content[0].category").value("FOOD"))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        @WithMockUser
        @DisplayName("given filter parameters when listing then applies filters")
        void given_filter_parameters_when_listing_then_applies_filters() throws Exception {
            // given
            var page = TransactionPage.of(List.of(), 0, 10, 0);
            given(getTransactionsUseCase.getTransactions(any())).willReturn(page);

            // when/then
            mockMvc.perform(get(LIST_ENDPOINT)
                            .param("iban", "PL61109010140000071219812874")
                            .param("category", "FOOD")
                            .param("from", "2024-01-01")
                            .param("to", "2024-01-31")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("given no transactions when listing then returns empty page")
        void given_no_transactions_when_listing_then_returns_empty() throws Exception {
            // given
            var page = TransactionPage.of(List.of(), 0, 20, 0);
            given(getTransactionsUseCase.getTransactions(any())).willReturn(page);

            // when/then
            mockMvc.perform(get(LIST_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }
}
