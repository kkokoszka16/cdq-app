package com.banking.application.service;

import com.banking.application.dto.TransactionFilter;
import com.banking.application.dto.TransactionPage;
import com.banking.application.dto.TransactionView;
import com.banking.application.port.in.GetTransactionsUseCase;
import com.banking.application.port.out.TransactionRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service implementing transaction query use case.
 */
@RequiredArgsConstructor
public class TransactionQueryService implements GetTransactionsUseCase {

    private final TransactionRepository transactionRepository;

    @Override
    public TransactionPage getTransactions(TransactionFilter filter) {
        var transactions = transactionRepository.findByFilters(
                filter.iban(),
                filter.category(),
                filter.from(),
                filter.to(),
                filter.page(),
                filter.size()
        );

        var totalCount = transactionRepository.countByFilters(
                filter.iban(),
                filter.category(),
                filter.from(),
                filter.to()
        );

        var views = transactions.stream()
                .map(TransactionView::from)
                .toList();

        return TransactionPage.of(views, filter.page(), filter.size(), totalCount);
    }
}
