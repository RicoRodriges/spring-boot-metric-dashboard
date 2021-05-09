package com.github.ricorodriges.metrics.extractor;

import com.github.ricorodriges.metrics.model.MeterData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.buildPercentileTable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimedMetricExtractor {

    public static List<TimedMetricResult> extractResults(Collection<MeterData> meters,
                                                         Predicate<MeterData> isTimed) {
        return meters.stream()
                .filter(isTimed)
                .map(m -> new TimedMetricResult(
                        getClassName(m), getMethodName(m), getExceptionName(m), buildPercentileTable(m, meters)))
                .collect(Collectors.toList());
    }

    private static String getClassName(MeterData m) {
        return m.getTags().get("class");
    }

    private static String getMethodName(MeterData m) {
        return m.getTags().get("method");
    }

    private static String getExceptionName(MeterData m) {
        String exception = m.getTags().get("exception");
        if ("none".equals(exception)) {
            exception = "";
        }
        return exception;
    }

    @Data
    @AllArgsConstructor
    public static class TimedMetricResult {
        private String className;
        private String method;
        private String exception;
        private PercentileTable percentileTable;
    }
}
