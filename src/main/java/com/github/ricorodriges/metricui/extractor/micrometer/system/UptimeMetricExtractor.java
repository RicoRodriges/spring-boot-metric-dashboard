package com.github.ricorodriges.metricui.extractor.micrometer.system;

import com.github.ricorodriges.metricui.model.MeterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;

import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.anyAsDate;
import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.anyAsDuration;

/**
 * @see io.micrometer.core.instrument.binder.system.UptimeMetrics
 */
@UtilityClass
public final class UptimeMetricExtractor {

    public static UptimeMetricResult extractResult(Collection<MeterData> meters) {
        ZonedDateTime startDate = anyAsDate(meters, "process.start.time").orElse(null);
        Duration uptime = anyAsDuration(meters, "process.uptime").orElse(null);
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
