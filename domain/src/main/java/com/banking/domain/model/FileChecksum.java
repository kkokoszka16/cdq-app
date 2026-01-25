package com.banking.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Value object representing SHA-256 checksum of file content.
 * Used for duplicate file detection.
 */
public record FileChecksum(String value) {

    private static final String ALGORITHM = "SHA-256";
    private static final int HEX_LENGTH = 64;

    public FileChecksum {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Checksum cannot be null or blank");
        }

        if (value.length() != HEX_LENGTH) {
            throw new IllegalArgumentException("Invalid checksum length: " + value.length());
        }
    }

    public static FileChecksum of(byte[] content) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }

        try {
            var digest = MessageDigest.getInstance(ALGORITHM);
            var hash = digest.digest(content);
            var hexString = HexFormat.of().formatHex(hash);
            return new FileChecksum(hexString);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm not available", exception);
        }
    }

    public static FileChecksum of(String content) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }

        return of(content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return value;
    }
}
