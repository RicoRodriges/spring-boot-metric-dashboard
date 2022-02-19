package com.github.ricorodriges.metricui.extractor.spring.jdbc;

import com.github.ricorodriges.metricui.model.MeterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.anyAsLong;
import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.findMetersByName;

/**
 * @see org.springframework.boot.actuate.metrics.jdbc.DataSourcePoolMetrics
 */
@UtilityClass
public final class DataSourceMetricExtractor {

    public static List<DataSourceMetricResult> extractResults(Collection<MeterData> meters) {
        final List<MeterData> dataSourceMeters = meters.stream()
                .filter(m -> m.getName().startsWith("jdbc.connections"))
                .collect(Collectors.toList());
        return findMetersByName(dataSourceMeters, "jdbc.connections.min")
                .map(m -> m.getTags().get("name"))
                .map(name -> {
                    Long min = anyAsLong(dataSourceMeters, "jdbc.connections.min", "name", name).orElse(null);
                    Long max = anyAsLong(dataSourceMeters, "jdbc.connections.max", "name", name).orElse(null);
                    Long idle = anyAsLong(dataSourceMeters, "jdbc.connections.idle", "name", name).orElse(null);
                    Long active = anyAsLong(dataSourceMeters, "jdbc.connections.active", "name", name).orElse(null);
                    return new DataSourceMetricResult(name, min, max, active, idle);
                })
                .collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    public static class DataSourceMetricResult {
        private String name;
        private Long minConnections;
        private Long maxConnections;
        private Long activeConnections;
        private Long idleConnections;
    }
}
