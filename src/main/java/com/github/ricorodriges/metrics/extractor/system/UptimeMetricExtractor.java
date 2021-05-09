package com.github.ricorodriges.metrics.extractor.system;

import com.github.ricorodriges.metrics.extractor.ExtractorUtils;
import com.github.ricorodriges.metrics.model.MeterData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;

import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByName;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UptimeMetricExtractor {

    public static UptimeMetricResult extractResult(Collection<MeterData> meters) {
        ZonedDateTime startDate = findMetersByName(meters, "process.start.time").findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .map(v -> ZonedDateTime.ofInstant(Instant.ofEpochSecond(v), ZoneOffset.UTC))
                .orElse(null);
        Duration uptime = findMetersByName(meters, "process.uptime").findAny()
                .flatMap(ExtractorUtils::getFirstValue)
                .map(v -> (long) (v * 1000))
                .map(Duration::ofMillis)
                .orElse(null);
        if (startDate == null || uptime == null) {
            return null;
        }
        return new UptimeMetricResult(startDate, uptime);
    }

    @Data
    @AllArgsConstructor
    public static class UptimeMetricResult {
        private ZonedDateTime startDate;
        private Duration uptime;
    }
}
