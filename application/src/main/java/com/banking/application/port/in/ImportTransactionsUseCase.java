package com.banking.application.port.in;

import com.banking.application.dto.ImportCommand;
import com.banking.application.dto.ImportResult;

/**
 * Input port for importing transactions from CSV file.
 */
public interface ImportTransactionsUseCase {

    ImportResult importTransactions(ImportCommand command);
}
