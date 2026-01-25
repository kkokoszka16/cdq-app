package com.banking.domain.model;

import java.util.UUID;

/**
 * Value object representing unique transaction identifier.
 */
public record TransactionId(String value) {

    private static final int UUID_LENGTH = 36;

    public TransactionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TransactionId cannot be null or blank");
        }
    }

    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID().toString());
    }

    public static TransactionId of(String value) {
        return new TransactionId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
