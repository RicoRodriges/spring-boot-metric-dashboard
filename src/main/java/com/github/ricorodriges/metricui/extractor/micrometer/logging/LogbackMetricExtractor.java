package com.github.ricorodriges.metricui.extractor.micrometer.logging;

import com.github.ricorodriges.metricui.model.MeterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.findMetersByName;
import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.getFirstValueAsLong;

/**
 * @see io.micrometer.core.instrument.binder.logging.LogbackMetrics
 */
@UtilityClass
public final class LogbackMetricExtractor {

    public static LogbackMetricResult extractResult(Collection<MeterData> meters) {
        Map<String, Long> events = findMetersByName(meters, "logback.events")
                .collect(Collectors.toMap(
                        m -> m.getTags().get("level"),
                        m -> getFirstValueAsLong(m).orElse(0L)
                ));
        if (events.isEmpty()) {
            return null;
        }

        return new LogbackMetricResult(events);
    }

    @Data
    @AllArgsConstructor
    public static class LogbackMetricResult {
        private Map<String, Long> events;
    }
}
