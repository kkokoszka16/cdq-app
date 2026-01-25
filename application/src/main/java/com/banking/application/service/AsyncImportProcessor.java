package com.banking.application.service;

/**
 * Interface for asynchronous import processing.
 * Implementation resides in infrastructure layer to use Spring @Async.
 */
public interface AsyncImportProcessor {

    void processAsync(String batchId, byte[] content);
}
