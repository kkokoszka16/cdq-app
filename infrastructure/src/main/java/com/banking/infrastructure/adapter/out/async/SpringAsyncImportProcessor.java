package com.banking.infrastructure.adapter.out.async;

import com.banking.application.service.AsyncImportProcessor;
import com.banking.application.service.TransactionImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Spring @Async implementation of AsyncImportProcessor.
 */
@Slf4j
@Component
public class SpringAsyncImportProcessor implements AsyncImportProcessor {

    private final TransactionImportService importService;

    public SpringAsyncImportProcessor(@Lazy TransactionImportService importService) {
        this.importService = importService;
    }

    @Override
    @Async("importTaskExecutor")
    public void processAsync(String batchId, byte[] content) {
        log.info("Starting async processing for batch: {}", batchId);

        try {
            importService.processImport(batchId, content);
            log.info("Completed async processing for batch: {}", batchId);
        } catch (Exception exception) {
            log.error("Async processing failed for batch {}: {}", batchId, exception.getMessage(), exception);
        }
    }
}
