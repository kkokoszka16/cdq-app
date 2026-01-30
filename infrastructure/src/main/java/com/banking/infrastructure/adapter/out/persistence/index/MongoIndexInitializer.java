package com.banking.infrastructure.adapter.out.persistence.index;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Validates MongoDB indexes on application startup.
 *
 * <p>Ensures that required indexes exist for optimal query performance.
 * Logs warnings if expected indexes are missing.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MongoIndexInitializer {

    private static final String TRANSACTIONS_COLLECTION = "transactions";
    private static final String IMPORT_BATCHES_COLLECTION = "import_batches";

    private static final Set<String> EXPECTED_TRANSACTION_INDEXES = Set.of(
            "iban_date_idx",
            "category_date_idx",
            "date_idx",
            "import_batch_idx"
    );

    private static final Set<String> EXPECTED_IMPORT_BATCH_INDEXES = Set.of(
            "checksum_status_idx",
            "created_at_idx"
    );

    private final MongoTemplate mongoTemplate;

    /**
     * Validates indexes after application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateIndexes() {
        log.info("Validating MongoDB indexes...");

        validateCollectionIndexes(TRANSACTIONS_COLLECTION, EXPECTED_TRANSACTION_INDEXES);
        validateCollectionIndexes(IMPORT_BATCHES_COLLECTION, EXPECTED_IMPORT_BATCH_INDEXES);

        log.info("MongoDB index validation completed");
    }

    private void validateCollectionIndexes(String collectionName, Set<String> expectedIndexes) {
        if (!mongoTemplate.collectionExists(collectionName)) {
            log.debug("Collection {} does not exist yet, skipping index validation", collectionName);
            return;
        }

        List<IndexInfo> existingIndexes = mongoTemplate
                .indexOps(collectionName)
                .getIndexInfo();

        Set<String> existingIndexNames = extractIndexNames(existingIndexes);

        for (String expectedIndex : expectedIndexes) {
            if (existingIndexNames.contains(expectedIndex)) {
                log.debug("Index {} exists on collection {}", expectedIndex, collectionName);
            } else {
                log.warn("Expected index {} not found on collection {}", expectedIndex, collectionName);
            }
        }

        logIndexInfo(collectionName, existingIndexes);
    }

    private Set<String> extractIndexNames(List<IndexInfo> indexes) {
        return indexes.stream()
                .map(IndexInfo::getName)
                .collect(java.util.stream.Collectors.toSet());
    }

    private void logIndexInfo(String collectionName, List<IndexInfo> indexes) {
        log.debug("Collection {} has {} indexes:", collectionName, indexes.size());

        indexes.forEach(index ->
                log.debug("  - {}: {}", index.getName(), index.getIndexFields())
        );
    }
}
