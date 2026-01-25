package com.banking.application.port.in;

import com.banking.application.dto.TransactionFilter;
import com.banking.application.dto.TransactionPage;

/**
 * Input port for querying transactions with pagination and filtering.
 */
public interface GetTransactionsUseCase {

    TransactionPage getTransactions(TransactionFilter filter);
}
