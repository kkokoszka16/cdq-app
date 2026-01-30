package com.banking.infrastructure.adapter.out.cache;

import com.banking.application.port.out.CachePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Set;

/**
 * No-operation cache adapter for environments without Redis.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(RedisCacheAdapter.class)
public class NoOpCacheAdapter implements CachePort {

    @Override
    public void evictStatisticsCache(Set<YearMonth> affectedMonths) {
        log.trace("NoOp: evictStatisticsCache for months: {}", affectedMonths);
    }

    @Override
    public void evictAllStatisticsCache() {
        log.trace("NoOp: evictAllStatisticsCache");
    }
}
