package com.banking.infrastructure.adapter.in.web.dto;

import com.banking.application.dto.IbanStatistics;
import com.banking.application.dto.IbanStatistics.IbanSummary;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IbanStatisticsResponse")
class IbanStatisticsResponseTest {

    private static final YearMonth TEST_MONTH = YearMonth.of(2024, 5);
    private static final String TEST_IBAN = "PL61109010140000071219812874";

    @Nested
    @DisplayName("given IbanStatistics with data")
    class GivenIbanStatisticsWithData {

        @Test
        @DisplayName("when from called then maps all fields correctly")
        void given_statistics_when_from_then_maps_correctly() {
            // given
            var summaries = List.of(
                    new IbanSummary(
                            TEST_IBAN,
                            new BigDecimal("5000.00"),
                            new BigDecimal("-2000.00"),
                            new BigDecimal("3000.00")
                    ),
                    new IbanSummary(
                            "DE89370400440532013000",
                            new BigDecimal("1000.00"),
                            new BigDecimal("-500.00"),
                            new BigDecimal("500.00")
                    )
            );
            var statistics = new IbanStatistics(TEST_MONTH, summaries);

            // when
            var response = IbanStatisticsResponse.from(statistics);

            // then
            assertThat(response.month()).isEqualTo("2024-05");
            assertThat(response.ibans()).hasSize(2);
        }

        @Test
        @DisplayName("when from called then maps iban summaries correctly")
        void given_statistics_when_from_then_maps_summaries() {
            // given
            var summaries = List.of(
                    new IbanSummary(
                            TEST_IBAN,
                            new BigDecimal("5000.00"),
                            new BigDecimal("-2000.00"),
                            new BigDecimal("3000.00")
                    )
            );
            var statistics = new IbanStatistics(TEST_MONTH, summaries);

            // when
            var response = IbanStatisticsResponse.from(statistics);

            // then
            var ibanDto = response.ibans().get(0);
            assertThat(ibanDto.iban()).isEqualTo(TEST_IBAN);
            assertThat(ibanDto.totalIncome()).isEqualByComparingTo("5000.00");
            assertThat(ibanDto.totalExpense()).isEqualByComparingTo("-2000.00");
            assertThat(ibanDto.balance()).isEqualByComparingTo("3000.00");
        }
    }

    @Nested
    @DisplayName("given empty IbanStatistics")
    class GivenEmptyIbanStatistics {

        @Test
        @DisplayName("when from called then returns response with empty ibans")
        void given_empty_statistics_when_from_then_empty_ibans() {
            // given
            var statistics = IbanStatistics.empty(TEST_MONTH);

            // when
            var response = IbanStatisticsResponse.from(statistics);

            // then
            assertThat(response.month()).isEqualTo("2024-05");
            assertThat(response.ibans()).isEmpty();
        }
    }

    @Nested
    @DisplayName("given IbanSummaryDto record")
    class GivenIbanSummaryDto {

        @Test
        @DisplayName("when created then all fields accessible")
        void given_dto_when_created_then_fields_accessible() {
            // given
            var iban = "GB82WEST12345698765432";
            var totalIncome = new BigDecimal("10000.00");
            var totalExpense = new BigDecimal("-3000.00");
            var balance = new BigDecimal("7000.00");

            // when
            var dto = new IbanStatisticsResponse.IbanSummaryDto(
                    iban,
                    totalIncome,
                    totalExpense,
                    balance
            );

            // then
            assertThat(dto.iban()).isEqualTo(iban);
            assertThat(dto.totalIncome()).isEqualByComparingTo("10000.00");
            assertThat(dto.totalExpense()).isEqualByComparingTo("-3000.00");
            assertThat(dto.balance()).isEqualByComparingTo("7000.00");
        }

        @Test
        @DisplayName("when two dtos have same values then equals returns true")
        void given_same_values_when_equals_then_true() {
            // given
            var dto1 = new IbanStatisticsResponse.IbanSummaryDto(
                    TEST_IBAN,
                    new BigDecimal("100.00"),
                    new BigDecimal("-50.00"),
                    new BigDecimal("50.00")
            );
            var dto2 = new IbanStatisticsResponse.IbanSummaryDto(
                    TEST_IBAN,
                    new BigDecimal("100.00"),
                    new BigDecimal("-50.00"),
                    new BigDecimal("50.00")
            );

            // then
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }

        @Test
        @DisplayName("when balance is zero then correctly represented")
        void given_zero_balance_when_created_then_correct() {
            // given
            var dto = new IbanStatisticsResponse.IbanSummaryDto(
                    TEST_IBAN,
                    new BigDecimal("500.00"),
                    new BigDecimal("-500.00"),
                    BigDecimal.ZERO
            );

            // then
            assertThat(dto.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("given IbanStatisticsResponse record")
    class GivenIbanStatisticsResponseRecord {

        @Test
        @DisplayName("when created then all fields accessible")
        void given_response_when_created_then_fields_accessible() {
            // given
            var ibans = List.of(
                    new IbanStatisticsResponse.IbanSummaryDto(
                            TEST_IBAN,
                            new BigDecimal("1000.00"),
                            new BigDecimal("-200.00"),
                            new BigDecimal("800.00")
                    )
            );

            // when
            var response = new IbanStatisticsResponse("2024-07", ibans);

            // then
            assertThat(response.month()).isEqualTo("2024-07");
            assertThat(response.ibans()).hasSize(1);
        }

        @Test
        @DisplayName("when two responses have same values then equals returns true")
        void given_same_values_when_equals_then_true() {
            // given
            var ibans = List.of(
                    new IbanStatisticsResponse.IbanSummaryDto(
                            TEST_IBAN,
                            new BigDecimal("500.00"),
                            new BigDecimal("-100.00"),
                            new BigDecimal("400.00")
                    )
            );
            var response1 = new IbanStatisticsResponse("2024-02", ibans);
            var response2 = new IbanStatisticsResponse("2024-02", ibans);

            // then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }
    }
}
