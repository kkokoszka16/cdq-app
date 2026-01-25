package com.banking.infrastructure.config;

import com.banking.application.port.out.CachePort;
import com.banking.application.port.out.ImportBatchRepository;
import com.banking.application.port.out.TransactionRepository;
import com.banking.application.service.AsyncImportProcessor;
import com.banking.application.service.CsvParsingService;
import com.banking.application.service.StatisticsService;
import com.banking.application.service.TransactionImportService;
import com.banking.application.service.TransactionQueryService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for application layer services.
 */
@Configuration
public class ApplicationServiceConfig {

    @Bean
    public CsvParsingService csvParsingService() {
        return new CsvParsingService();
    }

    @Bean
    public TransactionImportService transactionImportService(
            ImportBatchRepository importBatchRepository,
            TransactionRepository transactionRepository,
            CsvParsingService csvParsingService,
            CachePort cachePort,
            AsyncImportProcessor asyncProcessor
    ) {
        return new TransactionImportService(
                importBatchRepository,
                transactionRepository,
                csvParsingService,
                cachePort,
                asyncProcessor
        );
    }

    @Bean
    public TransactionQueryService transactionQueryService(
            TransactionRepository transactionRepository
    ) {
        return new TransactionQueryService(transactionRepository);
    }

    @Bean
    public StatisticsService statisticsService(
            TransactionRepository transactionRepository
    ) {
        return new StatisticsService(transactionRepository);
    }
}
