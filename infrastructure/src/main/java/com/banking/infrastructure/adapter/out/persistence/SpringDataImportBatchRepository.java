package com.banking.infrastructure.adapter.out.persistence;

import com.banking.infrastructure.adapter.out.persistence.entity.ImportBatchDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for import batches.
 */
public interface SpringDataImportBatchRepository extends MongoRepository<ImportBatchDocument, String> {

    Optional<ImportBatchDocument> findByFileChecksumAndStatus(String fileChecksum, String status);

    boolean existsByFileChecksumAndStatusIn(String fileChecksum, List<String> statuses);
}
