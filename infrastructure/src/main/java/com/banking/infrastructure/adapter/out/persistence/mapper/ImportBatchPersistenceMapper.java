package com.banking.infrastructure.adapter.out.persistence.mapper;

import com.banking.domain.model.FileChecksum;
import com.banking.domain.model.ImportBatch;
import com.banking.domain.model.ImportBatch.ImportError;
import com.banking.domain.model.ImportStatus;
import com.banking.infrastructure.adapter.out.persistence.entity.ImportBatchDocument;
import com.banking.infrastructure.adapter.out.persistence.entity.ImportBatchDocument.ImportErrorDocument;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for ImportBatch domain entity and MongoDB document conversion.
 */
@Mapper(componentModel = "spring")
public interface ImportBatchPersistenceMapper {

    default ImportBatchDocument toDocument(ImportBatch batch) {
        var errors = batch.getErrors().stream()
                .map(error -> new ImportErrorDocument(error.rowNumber(), error.message()))
                .toList();

        return new ImportBatchDocument(
                batch.getId(),
                batch.getFilename(),
                batch.getFileChecksum().value(),
                batch.getStatus().name(),
                batch.getTotalRows(),
                batch.getSuccessCount(),
                batch.getErrorCount(),
                errors,
                batch.getCreatedAt(),
                batch.getCompletedAt()
        );
    }

    default ImportBatch toDomain(ImportBatchDocument document) {
        var errors = document.getErrors().stream()
                .map(error -> new ImportError(error.getRowNumber(), error.getMessage()))
                .toList();

        return ImportBatch.reconstitute(
                document.getId(),
                document.getFilename(),
                new FileChecksum(document.getFileChecksum()),
                ImportStatus.valueOf(document.getStatus()),
                document.getTotalRows(),
                document.getSuccessCount(),
                document.getErrorCount(),
                errors,
                document.getCreatedAt(),
                document.getCompletedAt()
        );
    }
}
