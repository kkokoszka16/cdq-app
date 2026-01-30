package com.banking.application.port.out;

import com.banking.domain.model.FileChecksum;
import com.banking.domain.model.ImportBatch;
import com.banking.domain.model.ImportStatus;

import java.util.Optional;

/**
 * Output port for import batch persistence operations.
 */
public interface ImportBatchRepository {

    void save(ImportBatch batch);

    Optional<ImportBatch> findById(String id);

    Optional<ImportBatch> findByChecksumAndStatus(FileChecksum checksum, ImportStatus status);

    boolean existsByChecksumAndStatusIn(FileChecksum checksum, ImportStatus... statuses);
}
