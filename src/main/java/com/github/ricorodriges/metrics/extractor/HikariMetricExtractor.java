package com.github.ricorodriges.metrics.extractor;

import com.github.ricorodriges.metrics.model.MeterData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByNameAndTag;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HikariMetricExtractor {

    public static List<HikariMetricResult> extractResults(Collection<MeterData> meters) {
        final List<MeterData> hikariMetrics = meters.stream()
                .filter(m -> m.getName().startsWith("hikaricp."))
                .collect(Collectors.toList());
        return hikariMetrics.stream()
                .map(HikariMetricExtractor::getPool)
                .distinct()
                .map(pool -> extractPool(hikariMetrics, pool))
                .collect(Collectors.toList());
    }

    private static HikariMetricResult extractPool(Collection<MeterData> meters, String pool) {
        long min = findMetersByNameAndTag(meters, "hikaricp.connections.min", "pool", pool)
                .findAny().flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long max = findMetersByNameAndTag(meters, "hikaricp.connections.max", "pool", pool)
                .findAny().flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long idle = findMetersByNameAndTag(meters, "hikaricp.connections.idle", "pool", pool)
                .findAny().flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long active = findMetersByNameAndTag(meters, "hikaricp.connections.active", "pool", pool)
                .findAny().flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long current = findMetersByNameAndTag(meters, "hikaricp.connections", "pool", pool)
                .findAny().flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long pendingThreads = findMetersByNameAndTag(meters, "hikaricp.connections.pending", "pool", pool)
                .findAny().flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long timeout = findMetersByNameAndTag(meters, "hikaricp.connections.timeout", "pool", pool)
                .findAny().flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);

        return new HikariMetricResult(pool, min, max, active, idle, current, pendingThreads, timeout,
                extractPercentile(meters, pool, "hikaricp.connections.creation"),
                extractPercentile(meters, pool, "hikaricp.connections.acquire"),
                extractPercentile(meters, pool, "hikaricp.connections.usage"));
    }

    private static PercentileTable extractPercentile(Collection<MeterData> meters, String pool,
                                                     String metricName) {
        return findMetersByNameAndTag(meters, metricName, "pool", pool)
                .findAny()
                .map(m -> ExtractorUtils.buildPercentileTable(m, meters))
                .orElse(null);
    }

    private static String getPool(MeterData m) {
        return m.getTags().get("pool");
    }

    @Data
    @AllArgsConstructor
    public static class HikariMetricResult {
        private String pool;
        private Long minConnections;
        private Long maxConnections;
        private Long activeConnections;
        private Long idleConnections;
        private Long totalConnections;
        private Long pendingThreads;
        private Long timeoutExceptions;

        private PercentileTable creation;
        private PercentileTable acquire;
        private PercentileTable usage;
    }
}
