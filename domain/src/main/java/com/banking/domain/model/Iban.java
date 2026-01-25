package com.banking.domain.model;

import com.banking.domain.exception.InvalidIbanException;

import java.util.regex.Pattern;

/**
 * Value object representing International Bank Account Number.
 * Validates format according to ISO 13616.
 */
public record Iban(String value) {

    private static final Pattern IBAN_PATTERN = Pattern.compile(
            "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$"
    );

    private static final int MIN_LENGTH = 15;
    private static final int MAX_LENGTH = 34;

    public Iban {
        validateIban(value);
    }

    public static Iban of(String value) {
        return new Iban(normalizeIban(value));
    }

    private static String normalizeIban(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\s+", "").toUpperCase();
    }

    private void validateIban(String iban) {
        if (iban == null || iban.isBlank()) {
            throw new InvalidIbanException("IBAN cannot be null or blank");
        }

        if (iban.length() < MIN_LENGTH || iban.length() > MAX_LENGTH) {
            throw new InvalidIbanException(
                    "IBAN must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters: " + iban
            );
        }

        if (!IBAN_PATTERN.matcher(iban).matches()) {
            throw new InvalidIbanException("Invalid IBAN format: " + iban);
        }

        if (!isValidChecksum(iban)) {
            throw new InvalidIbanException("Invalid IBAN checksum: " + iban);
        }
    }

    private boolean isValidChecksum(String iban) {
        var rearranged = iban.substring(4) + iban.substring(0, 4);
        var numericIban = convertToNumeric(rearranged);
        return mod97(numericIban) == 1;
    }

    private String convertToNumeric(String iban) {
        var result = new StringBuilder();

        for (char character : iban.toCharArray()) {
            if (Character.isLetter(character)) {
                result.append(Character.getNumericValue(character));
            } else {
                result.append(character);
            }
        }

        return result.toString();
    }

    private int mod97(String number) {
        var remainder = 0;

        for (int index = 0; index < number.length(); index++) {
            var digit = Character.getNumericValue(number.charAt(index));
            remainder = (remainder * 10 + digit) % 97;
        }

        return remainder;
    }

    @Override
    public String toString() {
        return value;
    }
}
