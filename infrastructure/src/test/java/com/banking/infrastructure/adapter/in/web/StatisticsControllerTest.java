package com.banking.infrastructure.adapter.in.web;

import com.banking.application.dto.CategoryStatistics;
import com.banking.application.dto.IbanStatistics;
import com.banking.application.dto.MonthlyStatistics;
import com.banking.application.port.in.GetStatisticsUseCase;
import com.banking.domain.model.Category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatisticsController.class)
@DisplayName("StatisticsController")
class StatisticsControllerTest {

    private static final String BY_CATEGORY_ENDPOINT = "/api/v1/statistics/by-category";
    private static final String BY_IBAN_ENDPOINT = "/api/v1/statistics/by-iban";
    private static final String BY_MONTH_ENDPOINT = "/api/v1/statistics/by-month";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetStatisticsUseCase getStatisticsUseCase;

    @Nested
    @DisplayName("GET /by-category")
    class ByCategoryEndpoint {

        @Test
        @WithMockUser
        @DisplayName("given transactions in month when getting statistics then returns category summaries")
        void given_transactions_when_getting_stats_then_returns_summaries() throws Exception {
            // given
            var month = YearMonth.of(2024, 1);
            var categories = List.of(
                    new CategoryStatistics.CategorySummary(Category.FOOD, new BigDecimal("-500.00"), 10),
                    new CategoryStatistics.CategorySummary(Category.SALARY, new BigDecimal("5000.00"), 1)
            );
            var statistics = new CategoryStatistics(month, categories);
            given(getStatisticsUseCase.getStatisticsByCategory(any())).willReturn(statistics);

            // when/then
            mockMvc.perform(get(BY_CATEGORY_ENDPOINT)
                            .param("month", "2024-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.month").value("2024-01"))
                    .andExpect(jsonPath("$.categories").isArray())
                    .andExpect(jsonPath("$.categories[0].category").value("FOOD"))
                    .andExpect(jsonPath("$.categories[0].totalAmount").value(-500.00))
                    .andExpect(jsonPath("$.categories[0].transactionCount").value(10))
                    .andExpect(jsonPath("$.categories[1].category").value("SALARY"))
                    .andExpect(jsonPath("$.categories[1].totalAmount").value(5000.00));
        }

        @Test
        @WithMockUser
        @DisplayName("given no transactions when getting statistics then returns empty categories")
        void given_no_transactions_when_getting_stats_then_returns_empty() throws Exception {
            // given
            var month = YearMonth.of(2024, 1);
            var statistics = CategoryStatistics.empty(month);
            given(getStatisticsUseCase.getStatisticsByCategory(any())).willReturn(statistics);

            // when/then
            mockMvc.perform(get(BY_CATEGORY_ENDPOINT)
                            .param("month", "2024-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.month").value("2024-01"))
                    .andExpect(jsonPath("$.categories").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("given invalid month format when getting statistics then returns 400")
        void given_invalid_month_when_getting_stats_then_returns_bad_request() throws Exception {
            // when/then
            mockMvc.perform(get(BY_CATEGORY_ENDPOINT)
                            .param("month", "invalid-month"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("given missing month parameter when getting statistics then returns 400")
        void given_missing_month_when_getting_stats_then_returns_bad_request() throws Exception {
            // when/then
            mockMvc.perform(get(BY_CATEGORY_ENDPOINT))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /by-iban")
    class ByIbanEndpoint {

        @Test
        @WithMockUser
        @DisplayName("given transactions when getting statistics then returns iban summaries")
        void given_transactions_when_getting_stats_then_returns_summaries() throws Exception {
            // given
            var month = YearMonth.of(2024, 1);
            var ibans = List.of(
                    new IbanStatistics.IbanSummary(
                            "PL61109010140000071219812874",
                            new BigDecimal("5000.00"),
                            new BigDecimal("-1500.00")
                    )
            );
            var statistics = new IbanStatistics(month, ibans);
            given(getStatisticsUseCase.getStatisticsByIban(any())).willReturn(statistics);

            // when/then
            mockMvc.perform(get(BY_IBAN_ENDPOINT)
                            .param("month", "2024-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.month").value("2024-01"))
                    .andExpect(jsonPath("$.ibans").isArray())
                    .andExpect(jsonPath("$.ibans[0].iban").value("PL61109010140000071219812874"))
                    .andExpect(jsonPath("$.ibans[0].totalIncome").value(5000.00))
                    .andExpect(jsonPath("$.ibans[0].totalExpense").value(-1500.00))
                    .andExpect(jsonPath("$.ibans[0].balance").value(3500.00));
        }

        @Test
        @WithMockUser
        @DisplayName("given no transactions when getting statistics then returns empty ibans")
        void given_no_transactions_when_getting_stats_then_returns_empty() throws Exception {
            // given
            var month = YearMonth.of(2024, 1);
            var statistics = IbanStatistics.empty(month);
            given(getStatisticsUseCase.getStatisticsByIban(any())).willReturn(statistics);

            // when/then
            mockMvc.perform(get(BY_IBAN_ENDPOINT)
                            .param("month", "2024-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ibans").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /by-month")
    class ByMonthEndpoint {

        @Test
        @WithMockUser
        @DisplayName("given transactions in year when getting statistics then returns monthly summaries")
        void given_transactions_when_getting_stats_then_returns_summaries() throws Exception {
            // given
            var year = 2024;
            var months = List.of(
                    new MonthlyStatistics.MonthlySummary(
                            YearMonth.of(2024, 1),
                            new BigDecimal("5000.00"),
                            new BigDecimal("-2000.00")
                    ),
                    new MonthlyStatistics.MonthlySummary(
                            YearMonth.of(2024, 2),
                            new BigDecimal("5500.00"),
                            new BigDecimal("-2500.00")
                    )
            );
            var statistics = new MonthlyStatistics(year, months);
            given(getStatisticsUseCase.getStatisticsByMonth(anyInt())).willReturn(statistics);

            // when/then
            mockMvc.perform(get(BY_MONTH_ENDPOINT)
                            .param("year", "2024"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.year").value(2024))
                    .andExpect(jsonPath("$.months").isArray())
                    .andExpect(jsonPath("$.months[0].month").value("2024-01"))
                    .andExpect(jsonPath("$.months[0].totalIncome").value(5000.00))
                    .andExpect(jsonPath("$.months[0].totalExpense").value(-2000.00))
                    .andExpect(jsonPath("$.months[0].balance").value(3000.00));
        }

        @Test
        @WithMockUser
        @DisplayName("given no transactions when getting statistics then returns empty months")
        void given_no_transactions_when_getting_stats_then_returns_empty() throws Exception {
            // given
            var year = 2024;
            var statistics = MonthlyStatistics.empty(year);
            given(getStatisticsUseCase.getStatisticsByMonth(anyInt())).willReturn(statistics);

            // when/then
            mockMvc.perform(get(BY_MONTH_ENDPOINT)
                            .param("year", "2024"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.months").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("given missing year parameter when getting statistics then returns 400")
        void given_missing_year_when_getting_stats_then_returns_bad_request() throws Exception {
            // when/then
            mockMvc.perform(get(BY_MONTH_ENDPOINT))
                    .andExpect(status().isBadRequest());
        }
    }
}
