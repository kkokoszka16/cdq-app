package com.banking.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Category")
class CategoryTest {

    @ParameterizedTest
    @CsvSource({
            "FOOD, FOOD",
            "food, FOOD",
            "Food, FOOD",
            "TRANSPORT, TRANSPORT",
            "transport, TRANSPORT",
            "SALARY, SALARY"
    })
    @DisplayName("given valid category string when parsing then returns correct category")
    void given_valid_category_string_when_parsing_then_returns_correct_category(String input, Category expected) {
        // when
        var result = Category.fromString(input);

        // then
        assertThat(result).isPresent().contains(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"INVALID", "unknown", "123"})
    @DisplayName("given invalid category string when parsing then returns empty")
    void given_invalid_category_string_when_parsing_then_returns_empty(String input) {
        // when
        var result = Category.fromString(input);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("given invalid category when using orThrow then throws exception")
    void given_invalid_category_when_using_or_throw_then_throws_exception() {
        // when/then
        assertThatThrownBy(() -> Category.fromStringOrThrow("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown category");
    }

    @Test
    @DisplayName("given category when getting display name then returns readable name")
    void given_category_when_getting_display_name_then_returns_readable_name() {
        // when/then
        assertThat(Category.FOOD.getDisplayName()).isEqualTo("Food & Groceries");
        assertThat(Category.TRANSPORT.getDisplayName()).isEqualTo("Transportation");
        assertThat(Category.SALARY.getDisplayName()).isEqualTo("Salary & Income");
    }
}
