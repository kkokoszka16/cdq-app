package com.banking.application.dto;

import com.banking.domain.model.Category;
import com.banking.domain.model.Iban;
import com.banking.domain.model.Money;

import java.time.LocalDate;
import java.util.Currency;

/**
 * Intermediate DTO representing a successfully parsed CSV row.
 */
public record ParsedTransaction(
        Iban iban,
        LocalDate date,
        Currency currency,
        Category category,
        Money amount
) {
}
