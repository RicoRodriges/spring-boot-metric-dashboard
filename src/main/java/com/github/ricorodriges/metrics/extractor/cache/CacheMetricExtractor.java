package com.github.ricorodriges.metrics.extractor.cache;

import com.github.ricorodriges.metrics.extractor.ExtractorUtils;
import com.github.ricorodriges.metrics.model.MeterData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByName;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheMetricExtractor {

    public static List<CacheMetricResult> extractResults(Collection<MeterData> meters) {
        return findMetersByName(meters, "cache.size")
                .map(CacheMetricExtractor::getCacheManager)
                .distinct()
                .flatMap(manager -> extractResultsByManager(meters, manager).stream())
                .collect(Collectors.toList());
    }

    private static List<CacheMetricResult> extractResultsByManager(Collection<MeterData> meters, String cacheManager) {
        return findMetersByName(meters, "cache.size")
                .filter(m -> cacheManager.equals(getCacheManager(m)))
                .map(CacheMetricExtractor::getCacheName)
                .map(cacheName -> {
                    Long size = findCacheMeterValue(meters, "cache.size", cacheManager, cacheName, null).orElse(null);
                    Long evictions = findCacheMeterValue(meters, "cache.evictions", cacheManager, cacheName, null).orElse(null);
                    Long puts = findCacheMeterValue(meters, "cache.puts", cacheManager, cacheName, null).orElse(null);
                    Long hits = findCacheMeterValue(meters, "cache.gets", cacheManager, cacheName, "hit").orElse(null);
                    Long misses = findCacheMeterValue(meters, "cache.gets", cacheManager, cacheName, "miss").orElse(null);
                    return new CacheMetricResult(cacheName, cacheManager, size, evictions, puts, hits, misses);
                })
                .collect(Collectors.toList());
    }

    private static Optional<Long> findCacheMeterValue(Collection<MeterData> meters,
                                                      String name,
                                                      String cacheManager, String cacheName, String result) {
        return findMetersByName(meters, name)
                .filter(m -> cacheManager.equals(getCacheManager(m)))
                .filter(m -> cacheName.equals(getCacheName(m)))
                .filter(m -> result == null || result.equals(m.getTags().get("result")))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong);
    }

    private static String getCacheManager(MeterData m) {
        return Optional.ofNullable(m.getTags().get("cacheManager")).orElse("");
    }

    private static String getCacheName(MeterData m) {
        return Optional.ofNullable(m.getTags().get("cache")).orElse("");
    }

    @Data
    @AllArgsConstructor
    public static class CacheMetricResult {
        private String cache;
        private String cacheManager;
        private Long size;
        private Long evictions;
        private Long puts;
        private Long hits;
        private Long misses;
    }
}
