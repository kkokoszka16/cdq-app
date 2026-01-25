package com.banking.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB configuration.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.banking.infrastructure.adapter.out.persistence")
@EnableMongoAuditing
public class MongoConfig {
}
