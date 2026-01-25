package com.banking;

import com.banking.domain.model.ImportStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration tests verifying complete application flow.
 * Uses @SpringBootTest with Testcontainers MongoDB.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Import Flow E2E Integration")
class ImportFlowIntegrationTest {

    private static final String POLISH_IBAN = "PL61109010140000071219812874";
    private static final String GERMAN_IBAN = "DE89370400440532013000";

    @Container
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
        registry.add("spring.cache.type", () -> "none");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.getDb().drop();
    }


    @Nested
    @DisplayName("Complete Import Flow")
    class CompleteImportFlow {

        @Test
        @WithMockUser
        @DisplayName("given_valid_csv_when_importing_then_transactions_are_queryable")
        void given_valid_csv_when_importing_then_transactions_are_queryable() throws Exception {
            // given
            var csvContent = createMultiRowCsv();
            var file = new MockMultipartFile(
                    "file",
                    "transactions.csv",
                    "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8)
            );

            // when - import file
            var importResult = mockMvc.perform(multipart("/api/v1/transactions/import")
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.importId").isNotEmpty())
                    .andExpect(jsonPath("$.status").value("PROCESSING"))
                    .andReturn();

            var importId = extractImportId(importResult);

            // then - wait for completion and verify status
            waitForImportCompletion(importId);

            mockMvc.perform(get("/api/v1/transactions/import/{importId}/status", importId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.successCount").value(4));

            // and - verify transactions are queryable
            mockMvc.perform(get("/api/v1/transactions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(4)))
                    .andExpect(jsonPath("$.totalElements").value(4));
        }

        @Test
        @WithMockUser
        @DisplayName("given_imported_transactions_when_filtering_by_iban_then_returns_matching")
        void given_imported_transactions_when_filtering_by_iban_then_returns_matching() throws Exception {
            // given
            importCsv(createMultiRowCsv());

            // when/then
            mockMvc.perform(get("/api/v1/transactions")
                            .param("iban", POLISH_IBAN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(3)));
        }

        @Test
        @WithMockUser
        @DisplayName("given_imported_transactions_when_filtering_by_category_then_returns_matching")
        void given_imported_transactions_when_filtering_by_category_then_returns_matching() throws Exception {
            // given
            importCsv(createMultiRowCsv());

            // when/then
            mockMvc.perform(get("/api/v1/transactions")
                            .param("category", "FOOD"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }

        @Test
        @WithMockUser
        @DisplayName("given_duplicate_file_when_importing_again_then_returns_existing_batch")
        void given_duplicate_file_when_importing_again_then_returns_existing_batch() throws Exception {
            // given
            var csvContent = createSimpleCsv();
            var file = new MockMultipartFile(
                    "file",
                    "dup.csv",
                    "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8)
            );

            var firstImportId = importCsv(csvContent);

            // when - import same file again
            mockMvc.perform(multipart("/api/v1/transactions/import")
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.importId").value(firstImportId))
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.message").value("File already imported"));
        }
    }


    @Nested
    @DisplayName("Statistics Flow")
    class StatisticsFlow {

        @Test
        @WithMockUser
        @DisplayName("given_imported_transactions_when_getting_category_stats_then_aggregates_correctly")
        void given_imported_transactions_when_getting_category_stats_then_aggregates_correctly() throws Exception {
            // given
            importCsv(createMultiRowCsv());
            var currentMonth = LocalDate.now().minusDays(1).toString().substring(0, 7);

            // when/then
            mockMvc.perform(get("/api/v1/statistics/by-category")
                            .param("month", currentMonth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.categories", hasSize(greaterThan(0))));
        }

        @Test
        @WithMockUser
        @DisplayName("given_imported_transactions_when_getting_iban_stats_then_aggregates_correctly")
        void given_imported_transactions_when_getting_iban_stats_then_aggregates_correctly() throws Exception {
            // given
            importCsv(createMultiRowCsv());
            var currentMonth = LocalDate.now().minusDays(1).toString().substring(0, 7);

            // when/then
            mockMvc.perform(get("/api/v1/statistics/by-iban")
                            .param("month", currentMonth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ibans", hasSize(2)));
        }

        @Test
        @WithMockUser
        @DisplayName("given_imported_transactions_when_getting_monthly_stats_then_aggregates_correctly")
        void given_imported_transactions_when_getting_monthly_stats_then_aggregates_correctly() throws Exception {
            // given
            importCsv(createMultiRowCsv());
            var currentYear = LocalDate.now().getYear();

            // when/then
            mockMvc.perform(get("/api/v1/statistics/by-month")
                            .param("year", String.valueOf(currentYear)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.months", hasSize(greaterThan(0))));
        }

        @Test
        @WithMockUser
        @DisplayName("given_no_transactions_in_month_when_getting_stats_then_returns_empty")
        void given_no_transactions_in_month_when_getting_stats_then_returns_empty() throws Exception {
            // when/then
            mockMvc.perform(get("/api/v1/statistics/by-category")
                            .param("month", "2020-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.categories", hasSize(0)));
        }
    }


    @Nested
    @DisplayName("Error Handling Flow")
    class ErrorHandlingFlow {

        @Test
        @WithMockUser
        @DisplayName("given_csv_with_errors_when_importing_then_records_errors_in_status")
        void given_csv_with_errors_when_importing_then_records_errors_in_status() throws Exception {
            // given
            var csvWithErrors = createCsvWithErrors();
            var importId = importCsv(csvWithErrors);

            // when/then
            mockMvc.perform(get("/api/v1/transactions/import/{importId}/status", importId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.successCount").value(1))
                    .andExpect(jsonPath("$.errorCount").value(2))
                    .andExpect(jsonPath("$.errors", hasSize(2)));
        }

        @Test
        @WithMockUser
        @DisplayName("given_non_existing_import_when_getting_status_then_returns_404")
        void given_non_existing_import_when_getting_status_then_returns_404() throws Exception {
            mockMvc.perform(get("/api/v1/transactions/import/{importId}/status", "non-existing-id"))
                    .andExpect(status().isNotFound());
        }
    }


    private String importCsv(String csvContent) throws Exception {
        var file = new MockMultipartFile(
                "file",
                "transactions.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        var result = mockMvc.perform(multipart("/api/v1/transactions/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isAccepted())
                .andReturn();

        var importId = extractImportId(result);
        waitForImportCompletion(importId);

        return importId;
    }

    private void waitForImportCompletion(String importId) throws Exception {
        int maxAttempts = 50;
        int attempt = 0;

        while (attempt < maxAttempts) {
            var statusResult = mockMvc.perform(get("/api/v1/transactions/import/{importId}/status", importId))
                    .andExpect(status().isOk())
                    .andReturn();

            var responseBody = statusResult.getResponse().getContentAsString();
            if (responseBody.contains("COMPLETED") || responseBody.contains("FAILED")) {
                return;
            }

            Thread.sleep(100);
            attempt++;
        }

        throw new AssertionError("Import did not complete in time");
    }

    private String extractImportId(MvcResult result) throws Exception {
        var responseBody = result.getResponse().getContentAsString();
        var startIndex = responseBody.indexOf("\"importId\":\"") + 12;
        var endIndex = responseBody.indexOf("\"", startIndex);
        return responseBody.substring(startIndex, endIndex);
    }

    private String createSimpleCsv() {
        var date = LocalDate.now().minusDays(1).toString();
        return """
                iban,date,currency,category,amount
                %s,%s,PLN,FOOD,-100.50
                """.formatted(POLISH_IBAN, date);
    }

    private String createMultiRowCsv() {
        var date1 = LocalDate.now().minusDays(1).toString();
        var date2 = LocalDate.now().minusDays(2).toString();

        return """
                iban,date,currency,category,amount
                %s,%s,PLN,FOOD,-100.50
                %s,%s,PLN,TRANSPORT,-50.00
                %s,%s,PLN,SALARY,5000.00
                %s,%s,EUR,FOOD,-200.00
                """.formatted(
                POLISH_IBAN, date1,
                POLISH_IBAN, date2,
                POLISH_IBAN, date1,
                GERMAN_IBAN, date1
        );
    }

    private String createCsvWithErrors() {
        var validDate = LocalDate.now().minusDays(1).toString();

        return """
                iban,date,currency,category,amount
                %s,%s,PLN,FOOD,-100.50
                INVALID_IBAN,%s,PLN,TRANSPORT,-50.00
                %s,%s,PLN,UNKNOWN_CAT,100.00
                """.formatted(
                POLISH_IBAN, validDate,
                validDate,
                POLISH_IBAN, validDate
        );
    }
}
