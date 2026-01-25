package com.banking.infrastructure.config;

import com.banking.application.port.out.CachePort;
import com.banking.application.port.out.ImportBatchRepository;
import com.banking.application.port.out.TransactionRepository;
import com.banking.application.service.AsyncImportProcessor;
import com.banking.application.service.CsvParsingService;
import com.banking.application.service.StatisticsService;
import com.banking.application.service.TransactionImportService;
import com.banking.application.service.TransactionQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApplicationServiceConfig")
@ExtendWith(MockitoExtension.class)
class ApplicationServiceConfigTest {

    @Mock
    private ImportBatchRepository importBatchRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CachePort cachePort;

    @Mock
    private AsyncImportProcessor asyncProcessor;

    private ApplicationServiceConfig config;

    @BeforeEach
    void setUp() {
        config = new ApplicationServiceConfig();
    }

    @Nested
    @DisplayName("given csvParsingService bean")
    class GivenCsvParsingServiceBean {

        @Test
        @DisplayName("when called then returns CsvParsingService instance")
        void given_config_when_csv_parsing_service_then_returns_instance() {
            // when
            var service = config.csvParsingService();

            // then
            assertThat(service).isNotNull();
            assertThat(service).isInstanceOf(CsvParsingService.class);
        }
    }

    @Nested
    @DisplayName("given transactionImportService bean")
    class GivenTransactionImportServiceBean {

        @Test
        @DisplayName("when called then returns TransactionImportService instance")
        void given_config_when_transaction_import_service_then_returns_instance() {
            // given
            var csvParsingService = config.csvParsingService();

            // when
            var service = config.transactionImportService(
                    importBatchRepository,
                    transactionRepository,
                    csvParsingService,
                    cachePort,
                    asyncProcessor
            );

            // then
            assertThat(service).isNotNull();
            assertThat(service).isInstanceOf(TransactionImportService.class);
        }
    }

    @Nested
    @DisplayName("given transactionQueryService bean")
    class GivenTransactionQueryServiceBean {

        @Test
        @DisplayName("when called then returns TransactionQueryService instance")
        void given_config_when_transaction_query_service_then_returns_instance() {
            // when
            var service = config.transactionQueryService(transactionRepository);

            // then
            assertThat(service).isNotNull();
            assertThat(service).isInstanceOf(TransactionQueryService.class);
        }
    }

    @Nested
    @DisplayName("given statisticsService bean")
    class GivenStatisticsServiceBean {

        @Test
        @DisplayName("when called then returns StatisticsService instance")
        void given_config_when_statistics_service_then_returns_instance() {
            // when
            var service = config.statisticsService(transactionRepository);

            // then
            assertThat(service).isNotNull();
            assertThat(service).isInstanceOf(StatisticsService.class);
        }
    }

    @Nested
    @DisplayName("given all beans")
    class GivenAllBeans {

        @Test
        @DisplayName("when created then each call returns new instance")
        void given_config_when_multiple_calls_then_new_instances() {
            // when
            var csv1 = config.csvParsingService();
            var csv2 = config.csvParsingService();

            // then
            assertThat(csv1).isNotSameAs(csv2);
        }
    }
}
