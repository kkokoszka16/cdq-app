package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.TransactionPage;
import com.banking.application.dto.TransactionView;
import com.banking.domain.model.Category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TransactionPageResponse DTO.
 */
@DisplayName("TransactionPageResponse")
class TransactionPageResponseTest {

    private static final String VALID_IBAN = "PL61109010140000071219812874";
    private static final LocalDate VALID_DATE = LocalDate.of(2024, 6, 15);
    private static final String VALID_BATCH_ID = "batch-123";

    @Nested
    @DisplayName("given record constructor")
    class GivenRecordConstructor {

        @Test
        @DisplayName("when created with valid values then all fields are set")
        void given_valid_values_when_created_then_all_fields_set() {
            // given
            var transactions = List.of(createTransactionDto("tx-1"), createTransactionDto("tx-2"));

            // when
            var response = new TransactionPageResponse(transactions, 0, 10, 100, 10);

            // then
            assertThat(response.content()).hasSize(2);
            assertThat(response.page()).isZero();
            assertThat(response.size()).isEqualTo(10);
            assertThat(response.totalElements()).isEqualTo(100);
            assertThat(response.totalPages()).isEqualTo(10);
        }

        @Test
        @DisplayName("when created with empty content then content is empty")
        void given_empty_content_when_created_then_content_empty() {
            // when
            var response = new TransactionPageResponse(List.of(), 0, 10, 0, 0);

            // then
            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("given from factory method")
    class GivenFromFactoryMethod {

        @Test
        @DisplayName("when converting from TransactionPage then maps all fields")
        void given_transaction_page_when_from_then_maps_all_fields() {
            // given
            var views = List.of(
                    createTransactionView("tx-view-1"),
                    createTransactionView("tx-view-2"),
                    createTransactionView("tx-view-3")
            );

            var page = new TransactionPage(views, 1, 20, 50, 3);

            // when
            var response = TransactionPageResponse.from(page);

            // then
            assertThat(response.content()).hasSize(3);
            assertThat(response.page()).isEqualTo(1);
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.totalElements()).isEqualTo(50);
            assertThat(response.totalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("when converting page with single element then content has single dto")
        void given_single_element_page_when_from_then_single_dto() {
            // given
            var views = List.of(createTransactionView("tx-single"));
            var page = new TransactionPage(views, 0, 10, 1, 1);

            // when
            var response = TransactionPageResponse.from(page);

            // then
            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).id()).isEqualTo("tx-single");
        }

        @Test
        @DisplayName("when converting empty page then content is empty")
        void given_empty_page_when_from_then_content_empty() {
            // given
            var page = new TransactionPage(List.of(), 0, 10, 0, 0);

            // when
            var response = TransactionPageResponse.from(page);

            // then
            assertThat(response.content()).isEmpty();
        }

        @Test
        @DisplayName("when converting page then maps transaction view fields to dto")
        void given_page_when_from_then_maps_transaction_fields() {
            // given
            var view = new TransactionView(
                    "tx-detailed",
                    VALID_IBAN,
                    VALID_DATE,
                    "EUR",
                    Category.ENTERTAINMENT,
                    new BigDecimal("75.00"),
                    VALID_BATCH_ID
            );

            var page = new TransactionPage(List.of(view), 0, 10, 1, 1);

            // when
            var response = TransactionPageResponse.from(page);

            // then
            var dto = response.content().get(0);
            assertThat(dto.id()).isEqualTo("tx-detailed");
            assertThat(dto.iban()).isEqualTo(VALID_IBAN);
            assertThat(dto.transactionDate()).isEqualTo(VALID_DATE);
            assertThat(dto.currency()).isEqualTo("EUR");
            assertThat(dto.category()).isEqualTo(Category.ENTERTAINMENT);
            assertThat(dto.amount()).isEqualByComparingTo(new BigDecimal("75.00"));
            assertThat(dto.importBatchId()).isEqualTo(VALID_BATCH_ID);
        }

        @Test
        @DisplayName("when converting page on last page then correct pagination")
        void given_last_page_when_from_then_correct_pagination() {
            // given
            var views = List.of(createTransactionView("tx-last"));
            var page = new TransactionPage(views, 9, 10, 91, 10);

            // when
            var response = TransactionPageResponse.from(page);

            // then
            assertThat(response.page()).isEqualTo(9);
            assertThat(response.totalPages()).isEqualTo(10);
        }
    }

    private TransactionDto createTransactionDto(String id) {
        return new TransactionDto(
                id,
                VALID_IBAN,
                VALID_DATE,
                "PLN",
                Category.FOOD,
                new BigDecimal("100.00"),
                VALID_BATCH_ID
        );
    }

    private TransactionView createTransactionView(String id) {
        return new TransactionView(
                id,
                VALID_IBAN,
                VALID_DATE,
                "PLN",
                Category.FOOD,
                new BigDecimal("100.00"),
                VALID_BATCH_ID
        );
    }
}
