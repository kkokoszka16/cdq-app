package com.banking.application.service;

import com.banking.application.dto.CsvParseResult;
import com.banking.application.dto.ImportCommand;
import com.banking.application.dto.ImportResult;
import com.banking.application.dto.ImportStatusView;
import com.banking.application.dto.ParsedTransaction;
import com.banking.application.port.in.GetImportStatusUseCase;
import com.banking.application.port.in.ImportTransactionsUseCase;
import com.banking.application.port.out.CachePort;
import com.banking.application.port.out.ImportBatchRepository;
import com.banking.application.port.out.TransactionRepository;
import com.banking.domain.model.FileChecksum;
import com.banking.domain.model.ImportBatch;
import com.banking.domain.model.ImportStatus;
import com.banking.domain.model.Transaction;
import com.banking.domain.model.TransactionId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementing transaction import use case.
 */
@Slf4j
@RequiredArgsConstructor
public class TransactionImportService implements ImportTransactionsUseCase, GetImportStatusUseCase {

    private final ImportBatchRepository importBatchRepository;
    private final TransactionRepository transactionRepository;
    private final CsvParsingService csvParsingService;
    private final CachePort cachePort;
    private final AsyncImportProcessor asyncProcessor;

    @Override
    public ImportResult importTransactions(ImportCommand command) {
        var checksum = FileChecksum.of(command.content());

        var duplicateCheck = checkForDuplicate(checksum);
        if (duplicateCheck.isPresent()) {
            return duplicateCheck.get();
        }

        var batchId = generateBatchId();
        var batch = ImportBatch.create(batchId, command.filename(), checksum);

        importBatchRepository.save(batch);

        asyncProcessor.processAsync(batchId, command.content());

        return ImportResult.started(batchId);
    }

    @Override
    public Optional<ImportStatusView> getStatus(String importId) {
        return importBatchRepository.findById(importId)
                .map(ImportStatusView::from);
    }

    private Optional<ImportResult> checkForDuplicate(FileChecksum checksum) {
        if (importBatchRepository.existsByChecksumAndStatusIn(checksum, ImportStatus.COMPLETED)) {
            var existing = importBatchRepository.findByChecksumAndStatus(checksum, ImportStatus.COMPLETED);
            return existing.map(batch -> ImportResult.duplicate(batch.getId()));
        }

        if (importBatchRepository.existsByChecksumAndStatusIn(checksum, ImportStatus.PROCESSING, ImportStatus.PENDING)) {
            var existing = importBatchRepository.findByChecksumAndStatus(checksum, ImportStatus.PROCESSING);
            if (existing.isPresent()) {
                return Optional.of(ImportResult.inProgress(existing.get().getId()));
            }

            var pending = importBatchRepository.findByChecksumAndStatus(checksum, ImportStatus.PENDING);
            if (pending.isPresent()) {
                return Optional.of(ImportResult.inProgress(pending.get().getId()));
            }
        }

        return Optional.empty();
    }

    private String generateBatchId() {
        return UUID.randomUUID().toString();
    }

    public void processImport(String batchId, byte[] content) {
        var batch = importBatchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalStateException("Batch not found: " + batchId));

        try {
            var parseResult = csvParsingService.parse(content);
            processParseResult(batch, parseResult);
        } catch (Exception exception) {
            handleProcessingFailure(batch, exception);
        }
    }

    private void processParseResult(ImportBatch batch, CsvParseResult parseResult) {
        batch.startProcessing(parseResult.totalRowsProcessed());
        importBatchRepository.save(batch);

        var transactions = convertToTransactions(parseResult.validTransactions(), batch.getId());
        var affectedMonths = extractAffectedMonths(transactions);

        saveTransactionsInBatches(transactions, batch);
        recordErrors(batch, parseResult);

        batch.complete();
        importBatchRepository.save(batch);

        cachePort.evictStatisticsCache(affectedMonths);
    }

    private List<Transaction> convertToTransactions(List<ParsedTransaction> parsed, String batchId) {
        return parsed.stream()
                .map(parsedTx -> new Transaction(
                        TransactionId.generate(),
                        parsedTx.iban(),
                        parsedTx.date(),
                        parsedTx.currency(),
                        parsedTx.category(),
                        parsedTx.amount(),
                        batchId
                ))
                .toList();
    }

    private Set<YearMonth> extractAffectedMonths(List<Transaction> transactions) {
        return transactions.stream()
                .map(tx -> YearMonth.from(tx.transactionDate()))
                .collect(Collectors.toSet());
    }

    private void saveTransactionsInBatches(List<Transaction> transactions, ImportBatch batch) {
        var batchSize = 100;

        for (int index = 0; index < transactions.size(); index += batchSize) {
            var end = Math.min(index + batchSize, transactions.size());
            var batchTransactions = transactions.subList(index, end);

            transactionRepository.saveAll(batchTransactions);

            for (int count = 0; count < batchTransactions.size(); count++) {
                batch.recordSuccess();
            }
        }
    }

    private void recordErrors(ImportBatch batch, CsvParseResult parseResult) {
        parseResult.errors().forEach(error ->
                batch.recordError(error.rowNumber(), error.message())
        );
    }

    private void handleProcessingFailure(ImportBatch batch, Exception exception) {
        log.error("Import failed for batch {}: {}", batch.getId(), exception.getMessage(), exception);
        batch.fail("Processing failed: " + exception.getMessage());
        importBatchRepository.save(batch);
    }
}
