package com.banking.infrastructure;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test configuration class for infrastructure integration tests.
 * Required by @DataMongoTest and other slice tests.
 */
@SpringBootApplication(scanBasePackages = "com.banking")
public class TestApplication {
}
