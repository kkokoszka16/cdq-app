package com.banking.application.service;

import com.banking.application.dto.CsvParseResult;
import com.banking.application.dto.ImportCommand;
import com.banking.application.dto.ParsedTransaction;
import com.banking.application.port.out.CachePort;
import com.banking.application.port.out.ImportBatchRepository;
import com.banking.application.port.out.TransactionRepository;
import com.banking.domain.model.Category;
import com.banking.domain.model.FileChecksum;
import com.banking.domain.model.Iban;
import com.banking.domain.model.ImportBatch;
import com.banking.domain.model.ImportStatus;
import com.banking.domain.model.Money;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionImportService")
class TransactionImportServiceTest {

    private static final String TEST_FILENAME = "transactions.csv";
    private static final byte[] TEST_CONTENT = "test content".getBytes(StandardCharsets.UTF_8);
    private static final String VALID_IBAN = "PL61109010140000071219812874";

    @Mock
    private ImportBatchRepository importBatchRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CsvParsingService csvParsingService;

    @Mock
    private CachePort cachePort;

    @Mock
    private AsyncImportProcessor asyncProcessor;

    @Captor
    private ArgumentCaptor<ImportBatch> batchCaptor;

    private TransactionImportService importService;

    @BeforeEach
    void setUp() {
        importService = new TransactionImportService(
                importBatchRepository,
                transactionRepository,
                csvParsingService,
                cachePort,
                asyncProcessor
        );
    }

    @Nested
    @DisplayName("importTransactions")
    class ImportTransactions {

        @Test
        @DisplayName("given new file when importing then creates batch and starts async processing")
        void given_new_file_when_importing_then_creates_batch_and_starts_async() {
            // given
            var command = new ImportCommand(TEST_FILENAME, TEST_CONTENT);
            given(importBatchRepository.existsByChecksumAndStatusIn(any(FileChecksum.class), any(ImportStatus.class)))
                    .willReturn(false);

            // when
            var result = importService.importTransactions(command);

            // then
            assertThat(result.status()).isEqualTo(ImportStatus.PROCESSING);
            assertThat(result.message()).isEqualTo("Import started");
            assertThat(result.importId()).isNotBlank();

            then(importBatchRepository).should().save(batchCaptor.capture());
            var savedBatch = batchCaptor.getValue();
            assertThat(savedBatch.getFilename()).isEqualTo(TEST_FILENAME);
            assertThat(savedBatch.getStatus()).isEqualTo(ImportStatus.PENDING);

            then(asyncProcessor).should().processAsync(anyString(), any(byte[].class));
        }

        @Test
        @DisplayName("given already imported file when importing then returns duplicate result")
        void given_already_imported_file_when_importing_then_returns_duplicate() {
            // given
            var command = new ImportCommand(TEST_FILENAME, TEST_CONTENT);
            var checksum = FileChecksum.of(TEST_CONTENT);
            var existingBatch = ImportBatch.create("existing-id", TEST_FILENAME, checksum);

            given(importBatchRepository.existsByChecksumAndStatusIn(any(FileChecksum.class), any(ImportStatus.class)))
                    .willReturn(true);
            given(importBatchRepository.findByChecksumAndStatus(any(FileChecksum.class), any(ImportStatus.class)))
                    .willReturn(Optional.of(existingBatch));

            // when
            var result = importService.importTransactions(command);

            // then
            assertThat(result.status()).isEqualTo(ImportStatus.COMPLETED);
            assertThat(result.message()).isEqualTo("File already imported");
            assertThat(result.importId()).isEqualTo("existing-id");

            then(asyncProcessor).should(never()).processAsync(anyString(), any(byte[].class));
        }

        @Test
        @DisplayName("given file being processed when importing then returns in progress result")
        void given_file_being_processed_when_importing_then_returns_in_progress() {
            // given
            var command = new ImportCommand(TEST_FILENAME, TEST_CONTENT);
            var checksum = FileChecksum.of(TEST_CONTENT);
            var processingBatch = ImportBatch.create("processing-id", TEST_FILENAME, checksum);

            given(importBatchRepository.existsByChecksumAndStatusIn(any(FileChecksum.class), any(ImportStatus[].class)))
                    .willAnswer(invocation -> {
                        Object[] args = invocation.getArguments();
                        if (args.length > 1) {
                            ImportStatus firstStatus = (ImportStatus) args[1];
                            return firstStatus != ImportStatus.COMPLETED;
                        }
                        return false;
                    });
            given(importBatchRepository.findByChecksumAndStatus(any(FileChecksum.class), any(ImportStatus.class)))
                    .willReturn(Optional.of(processingBatch));

            // when
            var result = importService.importTransactions(command);

            // then
            assertThat(result.status()).isEqualTo(ImportStatus.PROCESSING);
            assertThat(result.message()).isEqualTo("Import already in progress");
        }
    }

    @Nested
    @DisplayName("getStatus")
    class GetStatus {

        @Test
        @DisplayName("given existing batch when getting status then returns status view")
        void given_existing_batch_when_getting_status_then_returns_view() {
            // given
            var batchId = "test-batch-id";
            var checksum = FileChecksum.of(TEST_CONTENT);
            var batch = ImportBatch.create(batchId, TEST_FILENAME, checksum);

            given(importBatchRepository.findById(batchId)).willReturn(Optional.of(batch));

            // when
            var result = importService.getStatus(batchId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().importId()).isEqualTo(batchId);
            assertThat(result.get().filename()).isEqualTo(TEST_FILENAME);
            assertThat(result.get().status()).isEqualTo(ImportStatus.PENDING);
        }

        @Test
        @DisplayName("given non-existing batch when getting status then returns empty")
        void given_non_existing_batch_when_getting_status_then_returns_empty() {
            // given
            var batchId = "non-existing-id";
            given(importBatchRepository.findById(batchId)).willReturn(Optional.empty());

            // when
            var result = importService.getStatus(batchId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("processImport")
    class ProcessImport {

        @Test
        @DisplayName("given valid CSV when processing then saves transactions and completes batch")
        void given_valid_csv_when_processing_then_saves_transactions_and_completes() {
            // given
            var batchId = "test-batch-id";
            var checksum = FileChecksum.of(TEST_CONTENT);
            var batch = ImportBatch.create(batchId, TEST_FILENAME, checksum);

            var parsedTransaction = new ParsedTransaction(
                    Iban.of(VALID_IBAN),
                    LocalDate.of(2024, 1, 15),
                    Currency.getInstance("PLN"),
                    Category.FOOD,
                    Money.of("-100.00")
            );
            var parseResult = new CsvParseResult(List.of(parsedTransaction), List.of(), 1);

            given(importBatchRepository.findById(batchId)).willReturn(Optional.of(batch));
            given(csvParsingService.parse(TEST_CONTENT)).willReturn(parseResult);

            // when
            importService.processImport(batchId, TEST_CONTENT);

            // then
            then(transactionRepository).should().saveAll(any());
            then(importBatchRepository).should(times(2)).save(any(ImportBatch.class));
            then(cachePort).should().evictStatisticsCache(any());
        }

        @Test
        @DisplayName("given CSV with errors when processing then records errors in batch")
        void given_csv_with_errors_when_processing_then_records_errors() {
            // given
            var batchId = "test-batch-id";
            var checksum = FileChecksum.of(TEST_CONTENT);
            var batch = ImportBatch.create(batchId, TEST_FILENAME, checksum);

            var error = new CsvParseResult.ParseError(3, "Invalid IBAN");
            var parseResult = new CsvParseResult(List.of(), List.of(error), 1);

            given(importBatchRepository.findById(batchId)).willReturn(Optional.of(batch));
            given(csvParsingService.parse(TEST_CONTENT)).willReturn(parseResult);

            // when
            importService.processImport(batchId, TEST_CONTENT);

            // then
            then(importBatchRepository).should(times(2)).save(batchCaptor.capture());
            var finalBatch = batchCaptor.getAllValues().get(1);
            assertThat(finalBatch.getErrorCount()).isEqualTo(1);
            assertThat(finalBatch.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("given batch not found when processing then throws exception")
        void given_batch_not_found_when_processing_then_throws_exception() {
            // given
            var batchId = "non-existing-id";
            given(importBatchRepository.findById(batchId)).willReturn(Optional.empty());

            // when/then
            org.assertj.core.api.Assertions.assertThatThrownBy(
                            () -> importService.processImport(batchId, TEST_CONTENT)
                    )
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Batch not found");
        }

        @Test
        @DisplayName("given exception during processing when processing then marks batch as failed")
        void given_exception_during_processing_when_processing_then_marks_failed() {
            // given
            var batchId = "test-batch-id";
            var checksum = FileChecksum.of(TEST_CONTENT);
            var batch = ImportBatch.create(batchId, TEST_FILENAME, checksum);

            given(importBatchRepository.findById(batchId)).willReturn(Optional.of(batch));
            given(csvParsingService.parse(TEST_CONTENT)).willThrow(new RuntimeException("Parse error"));

            // when
            importService.processImport(batchId, TEST_CONTENT);

            // then
            then(importBatchRepository).should().save(batchCaptor.capture());
            var failedBatch = batchCaptor.getValue();
            assertThat(failedBatch.getStatus()).isEqualTo(ImportStatus.FAILED);
        }
    }
}
