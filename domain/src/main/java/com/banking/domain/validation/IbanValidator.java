package com.banking.domain.validation;

import com.banking.domain.exception.InvalidIbanException;
import com.banking.domain.model.Iban;

import java.util.Optional;

/**
 * Utility class for IBAN validation operations.
 */
public final class IbanValidator {

    private IbanValidator() {
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            Iban.of(value);
            return true;
        } catch (InvalidIbanException exception) {
            return false;
        }
    }

    public static Optional<Iban> tryParse(String value) {
        try {
            return Optional.of(Iban.of(value));
        } catch (InvalidIbanException exception) {
            return Optional.empty();
        }
    }

    public static Iban parseOrThrow(String value, String contextMessage) {
        try {
            return Iban.of(value);
        } catch (InvalidIbanException exception) {
            throw new InvalidIbanException(contextMessage + ": " + exception.getMessage());
        }
    }
}
