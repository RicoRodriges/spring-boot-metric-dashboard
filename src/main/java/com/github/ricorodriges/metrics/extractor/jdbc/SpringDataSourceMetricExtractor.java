package com.github.ricorodriges.metrics.extractor.jdbc;

import com.github.ricorodriges.metrics.extractor.ExtractorUtils;
import com.github.ricorodriges.metrics.model.MeterData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByName;
import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByNameAndTag;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpringDataSourceMetricExtractor {

    public static List<SpringDataSourceMetricResult> extractResults(Collection<MeterData> meters) {
        return findMetersByName(meters, "jdbc.connections.min")
                .map(m -> m.getTags().get("name"))
                .distinct()
                .map(name -> {
                    Long min = findMetersByNameAndTag(meters, "jdbc.connections.min", "name", name)
                            .findAny()
                            .flatMap(ExtractorUtils::getFirstValueAsLong)
                            .orElse(null);
                    Long max = findMetersByNameAndTag(meters, "jdbc.connections.max", "name", name)
                            .findAny()
                            .flatMap(ExtractorUtils::getFirstValueAsLong)
                            .orElse(null);
                    Long idle = findMetersByNameAndTag(meters, "jdbc.connections.idle", "name", name)
                            .findAny()
                            .flatMap(ExtractorUtils::getFirstValueAsLong)
                            .orElse(null);
                    Long active = findMetersByNameAndTag(meters, "jdbc.connections.active", "name", name)
                            .findAny()
                            .flatMap(ExtractorUtils::getFirstValueAsLong)
                            .orElse(null);
                    return new SpringDataSourceMetricResult(name, min, max, active, idle);
                })
                .collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    public static class SpringDataSourceMetricResult {
        private String name;
        private Long minConnections;
        private Long maxConnections;
        private Long activeConnections;
        private Long idleConnections;
    }
}
