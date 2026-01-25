package com.banking.domain.exception;

/**
 * Exception thrown when IBAN validation fails.
 */
public final class InvalidIbanException extends DomainException {

    public InvalidIbanException(String message) {
        super(message);
    }
}
