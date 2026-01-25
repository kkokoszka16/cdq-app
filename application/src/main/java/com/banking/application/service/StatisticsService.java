package com.banking.application.service;

import com.banking.application.dto.CategoryStatistics;
import com.banking.application.dto.CategoryStatistics.CategorySummary;
import com.banking.application.dto.IbanStatistics;
import com.banking.application.dto.IbanStatistics.IbanSummary;
import com.banking.application.dto.MonthlyStatistics;
import com.banking.application.dto.MonthlyStatistics.MonthlySummary;
import com.banking.application.port.in.GetStatisticsUseCase;
import com.banking.application.port.out.TransactionRepository;
import com.banking.domain.model.Transaction;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementing statistics calculation use case.
 */
@RequiredArgsConstructor
public class StatisticsService implements GetStatisticsUseCase {

    private final TransactionRepository transactionRepository;

    @Override
    public CategoryStatistics getStatisticsByCategory(YearMonth month) {
        var transactions = transactionRepository.findByYearMonth(month.getYear(), month.getMonthValue());

        if (transactions.isEmpty()) {
            return CategoryStatistics.empty(month);
        }

        var summaries = calculateCategorySummaries(transactions);
        return new CategoryStatistics(month, summaries);
    }

    @Override
    public IbanStatistics getStatisticsByIban(YearMonth month) {
        var transactions = transactionRepository.findByYearMonth(month.getYear(), month.getMonthValue());

        if (transactions.isEmpty()) {
            return IbanStatistics.empty(month);
        }

        var summaries = calculateIbanSummaries(transactions);
        return new IbanStatistics(month, summaries);
    }

    @Override
    public MonthlyStatistics getStatisticsByMonth(int year) {
        var transactions = transactionRepository.findByYear(year);

        if (transactions.isEmpty()) {
            return MonthlyStatistics.empty(year);
        }

        var summaries = calculateMonthlySummaries(transactions);
        return new MonthlyStatistics(year, summaries);
    }

    private List<CategorySummary> calculateCategorySummaries(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::category))
                .entrySet().stream()
                .map(entry -> new CategorySummary(
                        entry.getKey(),
                        calculateTotalAmount(entry.getValue()),
                        entry.getValue().size()
                ))
                .sorted(Comparator.comparing(summary -> summary.category().name()))
                .toList();
    }

    private List<IbanSummary> calculateIbanSummaries(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(tx -> tx.iban().value()))
                .entrySet().stream()
                .map(entry -> createIbanSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(IbanSummary::iban))
                .toList();
    }

    private IbanSummary createIbanSummary(String iban, List<Transaction> transactions) {
        var income = calculateIncome(transactions);
        var expense = calculateExpense(transactions);
        return new IbanSummary(iban, income, expense);
    }

    private List<MonthlySummary> calculateMonthlySummaries(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(tx -> YearMonth.from(tx.transactionDate())))
                .entrySet().stream()
                .map(entry -> createMonthlySummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(MonthlySummary::month))
                .toList();
    }

    private MonthlySummary createMonthlySummary(YearMonth month, List<Transaction> transactions) {
        var income = calculateIncome(transactions);
        var expense = calculateExpense(transactions);
        return new MonthlySummary(month, income, expense);
    }

    private BigDecimal calculateTotalAmount(List<Transaction> transactions) {
        return transactions.stream()
                .map(tx -> tx.amount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateIncome(List<Transaction> transactions) {
        return transactions.stream()
                .filter(Transaction::isIncome)
                .map(tx -> tx.amount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateExpense(List<Transaction> transactions) {
        return transactions.stream()
                .filter(Transaction::isExpense)
                .map(tx -> tx.amount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
