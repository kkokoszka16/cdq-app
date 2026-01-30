package com.banking.infrastructure.adapter.out.persistence;

import com.banking.application.port.out.TransactionRepository;
import com.banking.domain.model.Category;
import com.banking.domain.model.Transaction;
import com.banking.infrastructure.adapter.out.persistence.entity.TransactionDocument;
import com.banking.infrastructure.adapter.out.persistence.mapper.TransactionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * MongoDB implementation of TransactionRepository.
 */
@Repository
@RequiredArgsConstructor
public class MongoTransactionRepository implements TransactionRepository {

    private final SpringDataTransactionRepository springDataRepository;
    private final MongoTemplate mongoTemplate;
    private final TransactionPersistenceMapper mapper;

    @Override
    public void save(Transaction transaction) {
        var document = mapper.toDocument(transaction);
        springDataRepository.save(document);
    }

    @Override
    public void saveAll(List<Transaction> transactions) {
        var documents = transactions.stream()
                .map(mapper::toDocument)
                .toList();
        springDataRepository.saveAll(documents);
    }

    @Override
    public List<Transaction> findByFilters(
            String iban,
            Category category,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        var query = buildFilterQuery(iban, category, from, to);
        query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate")));

        return mongoTemplate.find(query, TransactionDocument.class).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByFilters(
            String iban,
            Category category,
            LocalDate from,
            LocalDate to
    ) {
        var query = buildFilterQuery(iban, category, from, to);
        return mongoTemplate.count(query, TransactionDocument.class);
    }

    @Override
    public List<Transaction> findByDateRange(LocalDate from, LocalDate to) {
        return springDataRepository.findByTransactionDateBetween(from, to).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findByYearMonth(int year, int month) {
        var from = LocalDate.of(year, month, 1);
        var to = from.withDayOfMonth(from.lengthOfMonth());
        return findByDateRange(from, to);
    }

    @Override
    public List<Transaction> findByYear(int year) {
        var from = LocalDate.of(year, 1, 1);
        var to = LocalDate.of(year, 12, 31);
        return findByDateRange(from, to);
    }

    private Query buildFilterQuery(
            String iban,
            Category category,
            LocalDate from,
            LocalDate to
    ) {
        var query = new Query();

        if (iban != null && !iban.isBlank()) {
            query.addCriteria(Criteria.where("iban").is(iban));
        }

        if (category != null) {
            query.addCriteria(Criteria.where("category").is(category.name()));
        }

        if (from != null && to != null) {
            query.addCriteria(Criteria.where("transactionDate").gte(from).lte(to));
        } else if (from != null) {
            query.addCriteria(Criteria.where("transactionDate").gte(from));
        } else if (to != null) {
            query.addCriteria(Criteria.where("transactionDate").lte(to));
        }

        return query;
    }
}
