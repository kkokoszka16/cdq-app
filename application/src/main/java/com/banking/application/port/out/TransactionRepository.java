package com.banking.application.port.out;

import com.banking.domain.model.Category;
import com.banking.domain.model.Transaction;

import java.time.LocalDate;
import java.util.List;

/**
 * Output port for transaction persistence operations.
 */
public interface TransactionRepository {

    void save(Transaction transaction);

    void saveAll(List<Transaction> transactions);

    List<Transaction> findByFilters(
            String iban,
            Category category,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    );

    long countByFilters(
            String iban,
            Category category,
            LocalDate from,
            LocalDate to
    );

    List<Transaction> findByDateRange(LocalDate from, LocalDate to);

    List<Transaction> findByYearMonth(int year, int month);

    List<Transaction> findByYear(int year);
}
