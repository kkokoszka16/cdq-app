package com.banking.infrastructure.adapter.out.persistence;

import com.banking.application.port.out.ImportBatchRepository;
import com.banking.domain.model.FileChecksum;
import com.banking.domain.model.ImportBatch;
import com.banking.domain.model.ImportStatus;
import com.banking.infrastructure.adapter.out.persistence.mapper.ImportBatchPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;

/**
 * MongoDB implementation of ImportBatchRepository.
 */
@Repository
@RequiredArgsConstructor
public class MongoImportBatchRepository implements ImportBatchRepository {

    private final SpringDataImportBatchRepository springDataRepository;
    private final ImportBatchPersistenceMapper mapper;

    @Override
    public void save(ImportBatch batch) {
        var document = mapper.toDocument(batch);
        springDataRepository.save(document);
    }

    @Override
    public Optional<ImportBatch> findById(String id) {
        return springDataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<ImportBatch> findByChecksumAndStatus(FileChecksum checksum, ImportStatus status) {
        return springDataRepository.findByFileChecksumAndStatus(checksum.value(), status.name())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByChecksumAndStatusIn(FileChecksum checksum, ImportStatus... statuses) {
        var statusNames = Arrays.stream(statuses)
                .map(ImportStatus::name)
                .toList();

        return springDataRepository.existsByFileChecksumAndStatusIn(checksum.value(), statusNames);
    }
}
