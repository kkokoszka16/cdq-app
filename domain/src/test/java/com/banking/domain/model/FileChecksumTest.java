package com.banking.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileChecksum")
class FileChecksumTest {

    private static final int SHA256_HEX_LENGTH = 64;

    @Nested
    @DisplayName("given valid content")
    class GivenValidContent {

        @Test
        @DisplayName("when creating from byte array then generates SHA-256 checksum")
        void given_byte_array_when_creating_then_generates_sha256() {
            // given
            var content = "test content".getBytes(StandardCharsets.UTF_8);

            // when
            var checksum = FileChecksum.of(content);

            // then
            assertThat(checksum.value()).hasSize(SHA256_HEX_LENGTH);
            assertThat(checksum.value()).matches("[a-f0-9]{64}");
        }

        @Test
        @DisplayName("when creating from string then generates SHA-256 checksum")
        void given_string_when_creating_then_generates_sha256() {
            // given
            var content = "test content";

            // when
            var checksum = FileChecksum.of(content);

            // then
            assertThat(checksum.value()).hasSize(SHA256_HEX_LENGTH);
            assertThat(checksum.value()).matches("[a-f0-9]{64}");
        }

        @Test
        @DisplayName("when creating from same content then produces same checksum")
        void given_same_content_when_creating_then_produces_same_checksum() {
            // given
            var content = "identical content";

            // when
            var checksum1 = FileChecksum.of(content);
            var checksum2 = FileChecksum.of(content);

            // then
            assertThat(checksum1).isEqualTo(checksum2);
        }

        @Test
        @DisplayName("when creating from different content then produces different checksum")
        void given_different_content_when_creating_then_produces_different_checksum() {
            // given
            var content1 = "content A";
            var content2 = "content B";

            // when
            var checksum1 = FileChecksum.of(content1);
            var checksum2 = FileChecksum.of(content2);

            // then
            assertThat(checksum1).isNotEqualTo(checksum2);
        }

        @Test
        @DisplayName("when creating from known content then produces expected checksum")
        void given_known_content_when_creating_then_produces_expected_checksum() {
            // given
            var content = "hello";

            // when
            var checksum = FileChecksum.of(content);

            // then
            assertThat(checksum.value())
                    .isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
        }

        @Test
        @DisplayName("when calling toString then returns checksum value")
        void given_checksum_when_to_string_then_returns_value() {
            // given
            var checksum = FileChecksum.of("test");

            // when
            var result = checksum.toString();

            // then
            assertThat(result).isEqualTo(checksum.value());
        }
    }

    @Nested
    @DisplayName("given direct construction with valid hex")
    class GivenDirectConstruction {

        @Test
        @DisplayName("when creating with valid 64-char hex then succeeds")
        void given_valid_hex_when_creating_then_succeeds() {
            // given
            var validHex = "a".repeat(SHA256_HEX_LENGTH);

            // when
            var checksum = new FileChecksum(validHex);

            // then
            assertThat(checksum.value()).isEqualTo(validHex);
        }
    }

    @Nested
    @DisplayName("given invalid input")
    class GivenInvalidInput {

        @ParameterizedTest(name = "value: [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("when creating checksum from null/empty/blank then throws exception")
        void given_null_empty_blank_when_creating_then_throws_exception(String value) {
            // when/then
            assertThatThrownBy(() -> new FileChecksum(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or blank");
        }

        @Test
        @DisplayName("when creating with wrong length hex then throws exception")
        void given_wrong_length_hex_when_creating_then_throws_exception() {
            // given
            var shortHex = "abc123";

            // when/then
            assertThatThrownBy(() -> new FileChecksum(shortHex))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid checksum length");
        }

        @Test
        @DisplayName("when creating from null byte array then throws exception")
        void given_null_byte_array_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> FileChecksum.of((byte[]) null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or empty");
        }

        @Test
        @DisplayName("when creating from empty byte array then throws exception")
        void given_empty_byte_array_when_creating_then_throws_exception() {
            // given
            var emptyContent = new byte[0];

            // when/then
            assertThatThrownBy(() -> FileChecksum.of(emptyContent))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or empty");
        }

        @Test
        @DisplayName("when creating from null string then throws exception")
        void given_null_string_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> FileChecksum.of((String) null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or empty");
        }

        @Test
        @DisplayName("when creating from empty string then throws exception")
        void given_empty_string_when_creating_then_throws_exception() {
            // when/then
            assertThatThrownBy(() -> FileChecksum.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("when comparing equal checksums then equals returns true")
        void given_equal_checksums_when_comparing_then_returns_true() {
            // given
            var content = "same content";
            var checksum1 = FileChecksum.of(content);
            var checksum2 = FileChecksum.of(content);

            // when/then
            assertThat(checksum1).isEqualTo(checksum2);
            assertThat(checksum1.hashCode()).isEqualTo(checksum2.hashCode());
        }

        @Test
        @DisplayName("when comparing different checksums then equals returns false")
        void given_different_checksums_when_comparing_then_returns_false() {
            // given
            var checksum1 = FileChecksum.of("content A");
            var checksum2 = FileChecksum.of("content B");

            // when/then
            assertThat(checksum1).isNotEqualTo(checksum2);
        }
    }
}
