package com.banking.domain.exception;

/**
 * Exception thrown when monetary amount validation fails.
 */
public final class InvalidAmountException extends DomainException {

    public InvalidAmountException(String message) {
        super(message);
    }
}
