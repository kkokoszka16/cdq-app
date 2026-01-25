package com.banking.domain.exception;

/**
 * Base sealed class for all domain-level exceptions.
 * Provides type-safe exception hierarchy.
 */
public sealed class DomainException extends RuntimeException
        permits InvalidIbanException, InvalidAmountException, InvalidTransactionException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
