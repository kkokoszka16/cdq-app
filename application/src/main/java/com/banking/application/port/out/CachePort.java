package com.banking.application.port.out;

import java.time.YearMonth;
import java.util.Set;

/**
 * Output port for cache operations.
 */
public interface CachePort {

    void evictStatisticsCache(Set<YearMonth> affectedMonths);

    void evictAllStatisticsCache();
}
