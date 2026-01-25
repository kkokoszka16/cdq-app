package com.banking.domain.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enumeration of transaction categories for budget classification.
 */
public enum Category {

    FOOD("Food & Groceries"),
    TRANSPORT("Transportation"),
    UTILITIES("Bills & Utilities"),
    ENTERTAINMENT("Entertainment"),
    HEALTHCARE("Healthcare"),
    SHOPPING("Shopping"),
    SALARY("Salary & Income"),
    TRANSFER("Bank Transfer"),
    OTHER("Other");

    private static final Map<String, Category> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(
                    category -> category.name().toLowerCase(),
                    Function.identity()
            ));

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<Category> fromString(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(BY_NAME.get(value.trim().toLowerCase()));
    }

    public static Category fromStringOrThrow(String value) {
        return fromString(value)
                .orElseThrow(() -> new IllegalArgumentException("Unknown category: " + value));
    }
}
