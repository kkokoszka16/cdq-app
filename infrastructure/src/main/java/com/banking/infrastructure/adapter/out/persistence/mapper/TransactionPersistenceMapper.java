package com.banking.infrastructure.adapter.out.persistence.mapper;

import com.banking.domain.model.*;
import com.banking.infrastructure.adapter.out.persistence.entity.TransactionDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Currency;

/**
 * MapStruct mapper for Transaction domain entity and MongoDB document conversion.
 */
@Mapper(componentModel = "spring")
public interface TransactionPersistenceMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "iban", source = "iban.value")
    @Mapping(target = "currency", expression = "java(transaction.currency().getCurrencyCode())")
    @Mapping(target = "category", expression = "java(transaction.category().name())")
    @Mapping(target = "amount", source = "amount.amount")
    TransactionDocument toDocument(Transaction transaction);

    default Transaction toDomain(TransactionDocument document) {
        return new Transaction(
                TransactionId.of(document.getId()),
                new Iban(document.getIban()),
                document.getTransactionDate(),
                Currency.getInstance(document.getCurrency()),
                Category.valueOf(document.getCategory()),
                Money.of(document.getAmount()),
                document.getImportBatchId()
        );
    }
}
