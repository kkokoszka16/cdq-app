package com.banking.domain.exception;

/**
 * Exception thrown when transaction validation fails.
 */
public final class InvalidTransactionException extends DomainException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
