package com.banking.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * MongoDB document representing a transaction.
 */
@Document(collection = "transactions")
@CompoundIndexes({
        @CompoundIndex(name = "iban_date_idx", def = "{'iban': 1, 'transactionDate': -1}"),
        @CompoundIndex(name = "category_date_idx", def = "{'category': 1, 'transactionDate': -1}"),
        @CompoundIndex(name = "date_idx", def = "{'transactionDate': -1}"),
        @CompoundIndex(name = "import_batch_idx", def = "{'importBatchId': 1}")
})
public class TransactionDocument {

    @Id
    private String id;
    private String iban;
    private LocalDate transactionDate;
    private String currency;
    private String category;
    private BigDecimal amount;
    private String importBatchId;

    public TransactionDocument() {
    }

    public TransactionDocument(
            String id,
            String iban,
            LocalDate transactionDate,
            String currency,
            String category,
            BigDecimal amount,
            String importBatchId
    ) {
        this.id = id;
        this.iban = iban;
        this.transactionDate = transactionDate;
        this.currency = currency;
        this.category = category;
        this.amount = amount;
        this.importBatchId = importBatchId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getImportBatchId() {
        return importBatchId;
    }

    public void setImportBatchId(String importBatchId) {
        this.importBatchId = importBatchId;
    }
}
