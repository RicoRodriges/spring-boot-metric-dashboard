package com.github.ricorodriges.metricui.extractor.micrometer.cache;

import com.github.ricorodriges.metricui.extractor.ExtractorUtils;
import com.github.ricorodriges.metricui.model.MeterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.findMetersByName;

/**
 * @see io.micrometer.core.instrument.binder.cache.CacheMeterBinder
 */
@UtilityClass
public final class CacheMetricExtractor {

    public static List<CacheMetricResult> extractResults(Collection<MeterData> meters) {
        final List<MeterData> cacheMeters = meters.stream()
                .filter(m -> m.getName().startsWith("cache."))
                .collect(Collectors.toList());
        return findMetersByName(cacheMeters, "cache.size")
                .map(m -> {
                    final String cacheManager = getCacheManager(m);
                    final String cacheName = getCacheName(m);

                    Long size = findCacheMeterValue(cacheMeters, "cache.size", cacheManager, cacheName, null).orElse(null);
                    Long evictions = findCacheMeterValue(cacheMeters, "cache.evictions", cacheManager, cacheName, null).orElse(null);
                    Long puts = findCacheMeterValue(cacheMeters, "cache.puts", cacheManager, cacheName, null).orElse(null);
                    Long hits = findCacheMeterValue(cacheMeters, "cache.gets", cacheManager, cacheName, "hit").orElse(null);
                    Long misses = findCacheMeterValue(cacheMeters, "cache.gets", cacheManager, cacheName, "miss").orElse(null);
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
