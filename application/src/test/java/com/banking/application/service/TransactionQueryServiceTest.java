package com.banking.application.service;

import com.banking.application.dto.TransactionFilter;
import com.banking.application.port.out.TransactionRepository;
import com.banking.domain.model.Category;
import com.banking.domain.model.Iban;
import com.banking.domain.model.Money;
import com.banking.domain.model.Transaction;
import com.banking.domain.model.TransactionId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionQueryService")
class TransactionQueryServiceTest {

    private static final String BATCH_ID = "batch-001";
    private static final String POLISH_IBAN = "PL61109010140000071219812874";
    private static final Currency PLN = Currency.getInstance("PLN");

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new TransactionQueryService(transactionRepository);
    }

    @Nested
    @DisplayName("getTransactions")
    class GetTransactions {

        @Test
        @DisplayName("given transactions exist when querying then returns paginated results")
        void given_transactions_exist_when_querying_then_returns_paginated() {
            // given
            var filter = TransactionFilter.defaults();
            var transactions = List.of(
                    createTransaction("2024-01-15", Category.FOOD, "-100.00"),
                    createTransaction("2024-01-16", Category.TRANSPORT, "-50.00")
            );
            given(transactionRepository.findByFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(transactions);
            given(transactionRepository.countByFilters(any(), any(), any(), any()))
                    .willReturn(50L);

            // when
            var result = queryService.getTransactions(filter);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
            assertThat(result.totalElements()).isEqualTo(50);
            assertThat(result.totalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("given no transactions when querying then returns empty page")
        void given_no_transactions_when_querying_then_returns_empty() {
            // given
            var filter = TransactionFilter.defaults();
            given(transactionRepository.findByFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(List.of());
            given(transactionRepository.countByFilters(any(), any(), any(), any()))
                    .willReturn(0L);

            // when
            var result = queryService.getTransactions(filter);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("given filter with iban when querying then passes iban to repository")
        void given_filter_with_iban_when_querying_then_passes_to_repository() {
            // given
            var filter = TransactionFilter.defaults().withIban(POLISH_IBAN);
            given(transactionRepository.findByFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(List.of());
            given(transactionRepository.countByFilters(any(), any(), any(), any()))
                    .willReturn(0L);

            // when
            queryService.getTransactions(filter);

            // then
            then(transactionRepository).should().findByFilters(
                    org.mockito.ArgumentMatchers.eq(POLISH_IBAN),
                    any(), any(), any(), anyInt(), anyInt()
            );
        }

        @Test
        @DisplayName("given filter with category when querying then passes category to repository")
        void given_filter_with_category_when_querying_then_passes_to_repository() {
            // given
            var filter = TransactionFilter.defaults().withCategory(Category.FOOD);
            given(transactionRepository.findByFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(List.of());
            given(transactionRepository.countByFilters(any(), any(), any(), any()))
                    .willReturn(0L);

            // when
            queryService.getTransactions(filter);

            // then
            then(transactionRepository).should().findByFilters(
                    any(),
                    org.mockito.ArgumentMatchers.eq(Category.FOOD),
                    any(), any(), anyInt(), anyInt()
            );
        }

        @Test
        @DisplayName("given filter with date range when querying then passes dates to repository")
        void given_filter_with_date_range_when_querying_then_passes_to_repository() {
            // given
            var from = LocalDate.of(2024, 1, 1);
            var to = LocalDate.of(2024, 1, 31);
            var filter = TransactionFilter.defaults().withDateRange(from, to);
            given(transactionRepository.findByFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(List.of());
            given(transactionRepository.countByFilters(any(), any(), any(), any()))
                    .willReturn(0L);

            // when
            queryService.getTransactions(filter);

            // then
            then(transactionRepository).should().findByFilters(
                    any(), any(),
                    org.mockito.ArgumentMatchers.eq(from),
                    org.mockito.ArgumentMatchers.eq(to),
                    anyInt(), anyInt()
            );
        }

        @Test
        @DisplayName("given filter with pagination when querying then passes pagination to repository")
        void given_filter_with_pagination_when_querying_then_passes_to_repository() {
            // given
            var filter = TransactionFilter.defaults().withPagination(2, 50);
            given(transactionRepository.findByFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(List.of());
            given(transactionRepository.countByFilters(any(), any(), any(), any()))
                    .willReturn(0L);

            // when
            queryService.getTransactions(filter);

            // then
            then(transactionRepository).should().findByFilters(
                    any(), any(), any(), any(),
                    org.mockito.ArgumentMatchers.eq(2),
                    org.mockito.ArgumentMatchers.eq(50)
            );
        }

        @Test
        @DisplayName("given transactions when querying then maps to transaction views")
        void given_transactions_when_querying_then_maps_to_views() {
            // given
            var filter = TransactionFilter.defaults();
            var transaction = createTransaction("2024-01-15", Category.FOOD, "-100.00");
            given(transactionRepository.findByFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(List.of(transaction));
            given(transactionRepository.countByFilters(any(), any(), any(), any()))
                    .willReturn(1L);

            // when
            var result = queryService.getTransactions(filter);

            // then
            var view = result.content().getFirst();
            assertThat(view.id()).isEqualTo(transaction.id().value());
            assertThat(view.iban()).isEqualTo(POLISH_IBAN);
            assertThat(view.transactionDate()).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(view.currency()).isEqualTo("PLN");
            assertThat(view.category()).isEqualTo(Category.FOOD);
            assertThat(view.amount()).isEqualByComparingTo("-100.00");
            assertThat(view.importBatchId()).isEqualTo(BATCH_ID);
        }

        @Test
        @DisplayName("given page has next when querying then hasNext is true")
        void given_page_has_next_when_querying_then_has_next_true() {
            // given
            var filter = TransactionFilter.defaults().withPagination(0, 10);
            given(transactionRepository.findByFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(List.of(createTransaction("2024-01-15", Category.FOOD, "-100.00")));
            given(transactionRepository.countByFilters(any(), any(), any(), any()))
                    .willReturn(25L);

            // when
            var result = queryService.getTransactions(filter);

            // then
            assertThat(result.hasNext()).isTrue();
            assertThat(result.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("given page has previous when querying then hasPrevious is true")
        void given_page_has_previous_when_querying_then_has_previous_true() {
            // given
            var filter = TransactionFilter.defaults().withPagination(1, 10);
            given(transactionRepository.findByFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(List.of(createTransaction("2024-01-15", Category.FOOD, "-100.00")));
            given(transactionRepository.countByFilters(any(), any(), any(), any()))
                    .willReturn(15L);

            // when
            var result = queryService.getTransactions(filter);

            // then
            assertThat(result.hasPrevious()).isTrue();
            assertThat(result.hasNext()).isFalse();
        }
    }

    private Transaction createTransaction(String date, Category category, String amount) {
        return new Transaction(
                TransactionId.generate(),
                Iban.of(POLISH_IBAN),
                LocalDate.parse(date),
                PLN,
                category,
                Money.of(amount),
                BATCH_ID
        );
    }
}
