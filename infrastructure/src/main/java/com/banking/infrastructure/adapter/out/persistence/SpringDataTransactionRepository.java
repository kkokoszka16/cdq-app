package com.banking.infrastructure.adapter.out.persistence;

import com.banking.infrastructure.adapter.out.persistence.entity.TransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data MongoDB repository for transactions.
 */
public interface SpringDataTransactionRepository extends MongoRepository<TransactionDocument, String> {

    @Query("{ 'transactionDate': { $gte: ?0, $lte: ?1 } }")
    List<TransactionDocument> findByTransactionDateBetween(LocalDate from, LocalDate to);
}
