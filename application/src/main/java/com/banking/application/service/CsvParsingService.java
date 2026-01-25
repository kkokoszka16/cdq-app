package com.banking.application.service;

import com.banking.application.dto.CsvParseResult;
import com.banking.application.dto.CsvParseResult.ParseError;
import com.banking.application.dto.ParsedTransaction;
import com.banking.domain.exception.DomainException;
import com.banking.domain.model.Category;
import com.banking.domain.model.Iban;
import com.banking.domain.model.Money;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

/**
 * Service responsible for parsing CSV content into transaction data.
 */
public class CsvParsingService {

    private static final String DELIMITER = ",";
    private static final int EXPECTED_COLUMNS = 5;
    private static final int IBAN_INDEX = 0;
    private static final int DATE_INDEX = 1;
    private static final int CURRENCY_INDEX = 2;
    private static final int CATEGORY_INDEX = 3;
    private static final int AMOUNT_INDEX = 4;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int MAX_YEARS_IN_PAST = 10;

    public CsvParseResult parse(byte[] content) {
        var validTransactions = new ArrayList<ParsedTransaction>();
        var errors = new ArrayList<ParseError>();
        var rowNumber = 0;

        try (var reader = createReader(content)) {
            var headerLine = reader.readLine();

            if (headerLine == null || headerLine.isBlank()) {
                errors.add(new ParseError(0, "File is empty or has no header"));
                return new CsvParseResult(validTransactions, errors, 0);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                rowNumber++;

                if (isEmptyRow(line)) {
                    continue;
                }

                parseRow(line, rowNumber, validTransactions, errors);
            }
        } catch (IOException exception) {
            errors.add(new ParseError(0, "Failed to read file: " + exception.getMessage()));
        }

        return new CsvParseResult(validTransactions, errors, rowNumber);
    }

    private BufferedReader createReader(byte[] content) {
        var cleanedContent = stripBom(content);
        var inputStream = new ByteArrayInputStream(cleanedContent);
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    private byte[] stripBom(byte[] content) {
        if (content.length >= 3
                && content[0] == (byte) 0xEF
                && content[1] == (byte) 0xBB
                && content[2] == (byte) 0xBF) {
            var result = new byte[content.length - 3];
            System.arraycopy(content, 3, result, 0, result.length);
            return result;
        }
        return content;
    }

    private boolean isEmptyRow(String line) {
        return line == null || line.trim().isEmpty();
    }

    private void parseRow(
            String line,
            int rowNumber,
            List<ParsedTransaction> validTransactions,
            List<ParseError> errors
    ) {
        var columns = parseCsvLine(line);

        if (columns.length < EXPECTED_COLUMNS) {
            errors.add(new ParseError(rowNumber, "Insufficient columns: expected " + EXPECTED_COLUMNS));
            return;
        }

        try {
            var transaction = createTransaction(columns, rowNumber);
            validTransactions.add(transaction);
        } catch (DomainException | IllegalArgumentException exception) {
            errors.add(new ParseError(rowNumber, exception.getMessage()));
        }
    }

    private String[] parseCsvLine(String line) {
        var result = new ArrayList<String>();
        var current = new StringBuilder();
        var inQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            var character = line.charAt(index);

            if (character == '"') {
                inQuotes = !inQuotes;
            } else if (character == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(character);
            }
        }

        result.add(current.toString().trim());
        return result.toArray(String[]::new);
    }

    private ParsedTransaction createTransaction(String[] columns, int rowNumber) {
        var iban = parseIban(columns[IBAN_INDEX], rowNumber);
        var date = parseDate(columns[DATE_INDEX], rowNumber);
        var currency = parseCurrency(columns[CURRENCY_INDEX], rowNumber);
        var category = parseCategory(columns[CATEGORY_INDEX], rowNumber);
        var amount = parseAmount(columns[AMOUNT_INDEX], rowNumber);

        return new ParsedTransaction(iban, date, currency, category, amount);
    }

    private Iban parseIban(String value, int rowNumber) {
        var trimmed = value.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("IBAN is required at row " + rowNumber);
        }

        return Iban.of(trimmed);
    }

    private LocalDate parseDate(String value, int rowNumber) {
        var trimmed = value.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Date is required at row " + rowNumber);
        }

        try {
            var date = LocalDate.parse(trimmed, DATE_FORMAT);
            validateDateRange(date, rowNumber);
            return date;
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Invalid date format: " + value);
        }
    }

    private void validateDateRange(LocalDate date, int rowNumber) {
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date cannot be in the future: " + date);
        }

        var oldestAllowed = LocalDate.now().minusYears(MAX_YEARS_IN_PAST);
        if (date.isBefore(oldestAllowed)) {
            throw new IllegalArgumentException("Date cannot be older than " + MAX_YEARS_IN_PAST + " years: " + date);
        }
    }

    private Currency parseCurrency(String value, int rowNumber) {
        var trimmed = value.trim().toUpperCase();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Currency is required at row " + rowNumber);
        }

        try {
            return Currency.getInstance(trimmed);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid currency code: " + value);
        }
    }

    private Category parseCategory(String value, int rowNumber) {
        var trimmed = value.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Category is required at row " + rowNumber);
        }

        return Category.fromString(trimmed)
                .orElseThrow(() -> new IllegalArgumentException("Unknown category: " + value));
    }

    private Money parseAmount(String value, int rowNumber) {
        var trimmed = value.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Amount is required at row " + rowNumber);
        }

        return Money.of(trimmed);
    }
}
