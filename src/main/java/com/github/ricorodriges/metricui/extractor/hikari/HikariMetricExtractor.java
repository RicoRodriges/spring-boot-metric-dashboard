package com.github.ricorodriges.metricui.extractor.hikari;

import com.github.ricorodriges.metricui.extractor.ExtractorUtils;
import com.github.ricorodriges.metricui.extractor.PercentileTable;
import com.github.ricorodriges.metricui.model.MeterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.anyAsLong;
import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.findMetersByNameAndTag;

/**
 * @see com.zaxxer.hikari.metrics.micrometer.MicrometerMetricsTracker
 */
@UtilityClass
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
        long min = anyAsLong(meters, "hikaricp.connections.min", "pool", pool).orElse(0L);
        long max = anyAsLong(meters, "hikaricp.connections.max", "pool", pool).orElse(0L);
        long idle = anyAsLong(meters, "hikaricp.connections.idle", "pool", pool).orElse(0L);
        long active = anyAsLong(meters, "hikaricp.connections.active", "pool", pool).orElse(0L);
        long current = anyAsLong(meters, "hikaricp.connections", "pool", pool).orElse(0L);
        long pendingThreads = anyAsLong(meters, "hikaricp.connections.pending", "pool", pool).orElse(0L);
        long timeout = anyAsLong(meters, "hikaricp.connections.timeout", "pool", pool).orElse(0L);

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
