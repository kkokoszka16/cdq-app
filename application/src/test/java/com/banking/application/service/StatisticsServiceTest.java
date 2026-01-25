package com.banking.application.service;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService")
class StatisticsServiceTest {

    private static final String BATCH_ID = "batch-001";
    private static final String POLISH_IBAN = "PL61109010140000071219812874";
    private static final String GERMAN_IBAN = "DE89370400440532013000";
    private static final Currency PLN = Currency.getInstance("PLN");

    @Mock
    private TransactionRepository transactionRepository;

    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsService(transactionRepository);
    }

    @Nested
    @DisplayName("getStatisticsByCategory")
    class GetStatisticsByCategory {

        @Test
        @DisplayName("given transactions in month when getting statistics then returns category summaries")
        void given_transactions_when_getting_stats_then_returns_category_summaries() {
            // given
            var month = YearMonth.of(2024, 1);
            var transactions = List.of(
                    createTransaction(POLISH_IBAN, "2024-01-15", Category.FOOD, "-100.00"),
                    createTransaction(POLISH_IBAN, "2024-01-16", Category.FOOD, "-50.00"),
                    createTransaction(POLISH_IBAN, "2024-01-17", Category.TRANSPORT, "-30.00"),
                    createTransaction(POLISH_IBAN, "2024-01-20", Category.SALARY, "5000.00")
            );
            given(transactionRepository.findByYearMonth(2024, 1)).willReturn(transactions);

            // when
            var result = statisticsService.getStatisticsByCategory(month);

            // then
            assertThat(result.month()).isEqualTo(month);
            assertThat(result.categories()).hasSize(3);

            var foodStats = result.categories().stream()
                    .filter(s -> s.category() == Category.FOOD)
                    .findFirst()
                    .orElseThrow();
            assertThat(foodStats.totalAmount()).isEqualByComparingTo("-150.00");
            assertThat(foodStats.transactionCount()).isEqualTo(2);

            var salaryStats = result.categories().stream()
                    .filter(s -> s.category() == Category.SALARY)
                    .findFirst()
                    .orElseThrow();
            assertThat(salaryStats.totalAmount()).isEqualByComparingTo("5000.00");
            assertThat(salaryStats.transactionCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("given no transactions when getting statistics then returns empty result")
        void given_no_transactions_when_getting_stats_then_returns_empty() {
            // given
            var month = YearMonth.of(2024, 1);
            given(transactionRepository.findByYearMonth(2024, 1)).willReturn(List.of());

            // when
            var result = statisticsService.getStatisticsByCategory(month);

            // then
            assertThat(result.month()).isEqualTo(month);
            assertThat(result.categories()).isEmpty();
        }

        @Test
        @DisplayName("given transactions when getting statistics then categories are sorted alphabetically")
        void given_transactions_when_getting_stats_then_categories_sorted() {
            // given
            var month = YearMonth.of(2024, 1);
            var transactions = List.of(
                    createTransaction(POLISH_IBAN, "2024-01-15", Category.TRANSPORT, "-30.00"),
                    createTransaction(POLISH_IBAN, "2024-01-16", Category.FOOD, "-100.00"),
                    createTransaction(POLISH_IBAN, "2024-01-17", Category.ENTERTAINMENT, "-50.00")
            );
            given(transactionRepository.findByYearMonth(2024, 1)).willReturn(transactions);

            // when
            var result = statisticsService.getStatisticsByCategory(month);

            // then
            var categoryNames = result.categories().stream()
                    .map(s -> s.category().name())
                    .toList();
            assertThat(categoryNames).containsExactly("ENTERTAINMENT", "FOOD", "TRANSPORT");
        }
    }

    @Nested
    @DisplayName("getStatisticsByIban")
    class GetStatisticsByIban {

        @Test
        @DisplayName("given transactions when getting statistics then returns iban summaries")
        void given_transactions_when_getting_stats_then_returns_iban_summaries() {
            // given
            var month = YearMonth.of(2024, 1);
            var transactions = List.of(
                    createTransaction(POLISH_IBAN, "2024-01-15", Category.SALARY, "5000.00"),
                    createTransaction(POLISH_IBAN, "2024-01-16", Category.FOOD, "-100.00"),
                    createTransaction(GERMAN_IBAN, "2024-01-17", Category.SALARY, "3000.00")
            );
            given(transactionRepository.findByYearMonth(2024, 1)).willReturn(transactions);

            // when
            var result = statisticsService.getStatisticsByIban(month);

            // then
            assertThat(result.month()).isEqualTo(month);
            assertThat(result.ibans()).hasSize(2);

            var polishIbanStats = result.ibans().stream()
                    .filter(s -> s.iban().equals(POLISH_IBAN))
                    .findFirst()
                    .orElseThrow();
            assertThat(polishIbanStats.totalIncome()).isEqualByComparingTo("5000.00");
            assertThat(polishIbanStats.totalExpense()).isEqualByComparingTo("-100.00");
            assertThat(polishIbanStats.balance()).isEqualByComparingTo("4900.00");
        }

        @Test
        @DisplayName("given no transactions when getting statistics then returns empty result")
        void given_no_transactions_when_getting_stats_then_returns_empty() {
            // given
            var month = YearMonth.of(2024, 1);
            given(transactionRepository.findByYearMonth(2024, 1)).willReturn(List.of());

            // when
            var result = statisticsService.getStatisticsByIban(month);

            // then
            assertThat(result.month()).isEqualTo(month);
            assertThat(result.ibans()).isEmpty();
        }

        @Test
        @DisplayName("given only expenses when getting statistics then income is zero")
        void given_only_expenses_when_getting_stats_then_income_is_zero() {
            // given
            var month = YearMonth.of(2024, 1);
            var transactions = List.of(
                    createTransaction(POLISH_IBAN, "2024-01-15", Category.FOOD, "-100.00"),
                    createTransaction(POLISH_IBAN, "2024-01-16", Category.TRANSPORT, "-50.00")
            );
            given(transactionRepository.findByYearMonth(2024, 1)).willReturn(transactions);

            // when
            var result = statisticsService.getStatisticsByIban(month);

            // then
            var ibanStats = result.ibans().getFirst();
            assertThat(ibanStats.totalIncome()).isEqualByComparingTo("0.00");
            assertThat(ibanStats.totalExpense()).isEqualByComparingTo("-150.00");
            assertThat(ibanStats.balance()).isEqualByComparingTo("-150.00");
        }

        @Test
        @DisplayName("given transactions when getting statistics then ibans are sorted")
        void given_transactions_when_getting_stats_then_ibans_sorted() {
            // given
            var month = YearMonth.of(2024, 1);
            var transactions = List.of(
                    createTransaction(POLISH_IBAN, "2024-01-15", Category.FOOD, "-100.00"),
                    createTransaction(GERMAN_IBAN, "2024-01-16", Category.FOOD, "-50.00")
            );
            given(transactionRepository.findByYearMonth(2024, 1)).willReturn(transactions);

            // when
            var result = statisticsService.getStatisticsByIban(month);

            // then
            var ibans = result.ibans().stream().map(s -> s.iban()).toList();
            assertThat(ibans).containsExactly(GERMAN_IBAN, POLISH_IBAN);
        }
    }

    @Nested
    @DisplayName("getStatisticsByMonth")
    class GetStatisticsByMonth {

        @Test
        @DisplayName("given transactions in year when getting statistics then returns monthly summaries")
        void given_transactions_when_getting_stats_then_returns_monthly_summaries() {
            // given
            var year = 2024;
            var transactions = List.of(
                    createTransaction(POLISH_IBAN, "2024-01-15", Category.SALARY, "5000.00"),
                    createTransaction(POLISH_IBAN, "2024-01-20", Category.FOOD, "-500.00"),
                    createTransaction(POLISH_IBAN, "2024-02-15", Category.SALARY, "5000.00"),
                    createTransaction(POLISH_IBAN, "2024-02-20", Category.FOOD, "-600.00")
            );
            given(transactionRepository.findByYear(year)).willReturn(transactions);

            // when
            var result = statisticsService.getStatisticsByMonth(year);

            // then
            assertThat(result.year()).isEqualTo(year);
            assertThat(result.months()).hasSize(2);

            var janStats = result.months().stream()
                    .filter(s -> s.month().equals(YearMonth.of(2024, 1)))
                    .findFirst()
                    .orElseThrow();
            assertThat(janStats.totalIncome()).isEqualByComparingTo("5000.00");
            assertThat(janStats.totalExpense()).isEqualByComparingTo("-500.00");
            assertThat(janStats.balance()).isEqualByComparingTo("4500.00");

            var febStats = result.months().stream()
                    .filter(s -> s.month().equals(YearMonth.of(2024, 2)))
                    .findFirst()
                    .orElseThrow();
            assertThat(febStats.totalIncome()).isEqualByComparingTo("5000.00");
            assertThat(febStats.totalExpense()).isEqualByComparingTo("-600.00");
            assertThat(febStats.balance()).isEqualByComparingTo("4400.00");
        }

        @Test
        @DisplayName("given no transactions when getting statistics then returns empty result")
        void given_no_transactions_when_getting_stats_then_returns_empty() {
            // given
            var year = 2024;
            given(transactionRepository.findByYear(year)).willReturn(List.of());

            // when
            var result = statisticsService.getStatisticsByMonth(year);

            // then
            assertThat(result.year()).isEqualTo(year);
            assertThat(result.months()).isEmpty();
        }

        @Test
        @DisplayName("given transactions when getting statistics then months are sorted chronologically")
        void given_transactions_when_getting_stats_then_months_sorted() {
            // given
            var year = 2024;
            var transactions = List.of(
                    createTransaction(POLISH_IBAN, "2024-03-15", Category.FOOD, "-100.00"),
                    createTransaction(POLISH_IBAN, "2024-01-15", Category.FOOD, "-100.00"),
                    createTransaction(POLISH_IBAN, "2024-02-15", Category.FOOD, "-100.00")
            );
            given(transactionRepository.findByYear(year)).willReturn(transactions);

            // when
            var result = statisticsService.getStatisticsByMonth(year);

            // then
            var months = result.months().stream().map(s -> s.month()).toList();
            assertThat(months).containsExactly(
                    YearMonth.of(2024, 1),
                    YearMonth.of(2024, 2),
                    YearMonth.of(2024, 3)
            );
        }
    }

    private Transaction createTransaction(String iban, String date, Category category, String amount) {
        return new Transaction(
                TransactionId.generate(),
                Iban.of(iban),
                LocalDate.parse(date),
                PLN,
                category,
                Money.of(amount),
                BATCH_ID
        );
    }
}
