package com.banking.infrastructure.adapter.out.cache;

import com.banking.application.port.out.CachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Set;

/**
 * Redis cache adapter implementing CachePort.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
public class RedisCacheAdapter implements CachePort {

    private static final String CATEGORY_STATS_CACHE = "categoryStats";
    private static final String IBAN_STATS_CACHE = "ibanStats";
    private static final String MONTHLY_STATS_CACHE = "monthlyStats";

    private final CacheManager cacheManager;

    @Override
    public void evictStatisticsCache(Set<YearMonth> affectedMonths) {
        affectedMonths.forEach(this::evictCachesForMonth);

        var years = affectedMonths.stream()
                .map(YearMonth::getYear)
                .distinct()
                .toList();

        years.forEach(this::evictYearlyCache);

        log.debug("Evicted statistics cache for months: {}", affectedMonths);
    }

    @Override
    public void evictAllStatisticsCache() {
        evictCache(CATEGORY_STATS_CACHE);
        evictCache(IBAN_STATS_CACHE);
        evictCache(MONTHLY_STATS_CACHE);

        log.debug("Evicted all statistics caches");
    }

    private void evictCachesForMonth(YearMonth month) {
        evictFromCache(CATEGORY_STATS_CACHE, month.toString());
        evictFromCache(IBAN_STATS_CACHE, month.toString());
    }

    private void evictYearlyCache(int year) {
        evictFromCache(MONTHLY_STATS_CACHE, String.valueOf(year));
    }

    private void evictFromCache(String cacheName, String key) {
        var cache = cacheManager.getCache(cacheName);

        if (cache != null) {
            cache.evict(key);
        }
    }

    private void evictCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);

        if (cache != null) {
            cache.clear();
        }
    }
}
