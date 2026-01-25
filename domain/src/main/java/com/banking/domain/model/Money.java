package com.banking.domain.model;

import com.banking.domain.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object representing monetary amount.
 * Enforces precision of two decimal places.
 */
public record Money(BigDecimal amount) {

    private static final int DECIMAL_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public Money {
        validateAmount(amount);
        amount = normalizeAmount(amount);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money of(String amount) {
        if (amount == null || amount.isBlank()) {
            throw new InvalidAmountException("Amount cannot be null or blank");
        }

        try {
            return new Money(new BigDecimal(amount.trim()));
        } catch (NumberFormatException exception) {
            throw new InvalidAmountException("Invalid amount format: " + amount);
        }
    }

    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    private void validateAmount(BigDecimal value) {
        if (value == null) {
            throw new InvalidAmountException("Amount cannot be null");
        }

        if (value.compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidAmountException("Amount cannot be zero");
        }
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        return value.setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money negate() {
        return new Money(this.amount.negate());
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
