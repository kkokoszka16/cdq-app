package com.banking.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ImportCommand")
class ImportCommandTest {

    private static final String VALID_FILENAME = "transactions.csv";
    private static final byte[] VALID_CONTENT = "iban,date,currency,category,amount".getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("given valid parameters")
    class GivenValidParameters {

        @Test
        @DisplayName("when creating command then all fields are set correctly")
        void given_valid_params_when_creating_then_fields_set() {
            // when
            var command = new ImportCommand(VALID_FILENAME, VALID_CONTENT);

            // then
            assertThat(command.filename()).isEqualTo(VALID_FILENAME);
            assertThat(command.content()).isEqualTo(VALID_CONTENT);
        }

        @Test
        @DisplayName("when creating with various filenames then succeeds")
        void given_various_filenames_when_creating_then_succeeds() {
            // given/when/then
            assertThat(new ImportCommand("file.csv", VALID_CONTENT).filename()).isEqualTo("file.csv");
            assertThat(new ImportCommand("file-with-dash.csv", VALID_CONTENT).filename()).isEqualTo("file-with-dash.csv");
            assertThat(new ImportCommand("file_with_underscore.csv", VALID_CONTENT).filename()).isEqualTo("file_with_underscore.csv");
            assertThat(new ImportCommand("2024-01-15_export.csv", VALID_CONTENT).filename()).isEqualTo("2024-01-15_export.csv");
        }
    }

    @Nested
    @DisplayName("given invalid filename")
    class GivenInvalidFilename {

        @ParameterizedTest(name = "filename: [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("when creating with null/empty/blank filename then throws exception")
        void given_invalid_filename_when_creating_then_throws_exception(String filename) {
            // when/then
            assertThatThrownBy(() -> new ImportCommand(filename, VALID_CONTENT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Filename cannot be null or blank");
        }
    }

    @Nested
    @DisplayName("given invalid content")
    class GivenInvalidContent {

        @Test
        @DisplayName("when creating with null content then throws exception")
        void given_null_content_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> new ImportCommand(VALID_FILENAME, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Content cannot be null or empty");
        }

        @Test
        @DisplayName("when creating with empty content then throws exception")
        void given_empty_content_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> new ImportCommand(VALID_FILENAME, new byte[0]))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Content cannot be null or empty");
        }
    }
}
