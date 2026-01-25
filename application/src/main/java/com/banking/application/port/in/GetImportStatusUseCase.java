package com.banking.application.port.in;

import com.banking.application.dto.ImportStatusView;

import java.util.Optional;

/**
 * Input port for retrieving import batch status.
 */
public interface GetImportStatusUseCase {

    Optional<ImportStatusView> getStatus(String importId);
}
