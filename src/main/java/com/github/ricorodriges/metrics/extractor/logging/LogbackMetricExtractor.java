package com.github.ricorodriges.metrics.extractor.logging;

import com.github.ricorodriges.metrics.model.MeterData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByName;
import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.getFirstValueAsLong;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogbackMetricExtractor {

    public static LogbackMetricResult extractResult(Collection<MeterData> meters) {
        Map<String, Long> events = findMetersByName(meters, "logback.events")
                .collect(Collectors.toMap(
                        m -> m.getTags().get("level"),
                        m -> getFirstValueAsLong(m).orElse(0L)));
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
