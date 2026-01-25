package com.banking.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
public class RedisConfig {

    private static final String CATEGORY_STATS_CACHE = "categoryStats";
    private static final String IBAN_STATS_CACHE = "ibanStats";
    private static final String MONTHLY_STATS_CACHE = "monthlyStats";

    @Value("${cache.statistics-ttl-minutes:15}")
    private int statisticsTtlMinutes;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        var defaultConfig = createCacheConfiguration(Duration.ofMinutes(statisticsTtlMinutes));

        var cacheConfigurations = createCacheConfigurations();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    private RedisCacheConfiguration createCacheConfiguration(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .disableCachingNullValues();
    }

    private Map<String, RedisCacheConfiguration> createCacheConfigurations() {
        var statisticsTtl = Duration.ofMinutes(statisticsTtlMinutes);
        var config = createCacheConfiguration(statisticsTtl);

        var configurations = new HashMap<String, RedisCacheConfiguration>();
        configurations.put(CATEGORY_STATS_CACHE, config);
        configurations.put(IBAN_STATS_CACHE, config);
        configurations.put(MONTHLY_STATS_CACHE, config);

        return configurations;
    }
}
