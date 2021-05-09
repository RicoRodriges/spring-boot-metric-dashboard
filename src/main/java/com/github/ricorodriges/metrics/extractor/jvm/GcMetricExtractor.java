package com.github.ricorodriges.metrics.extractor.jvm;

import com.github.ricorodriges.metrics.extractor.ExtractorUtils;
import com.github.ricorodriges.metrics.extractor.PercentileTable;
import com.github.ricorodriges.metrics.extractor.jvm.GcMetricExtractor.GcMetricResult.ActionResult;
import com.github.ricorodriges.metrics.model.MeterData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.buildPercentileTable;
import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByName;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GcMetricExtractor {

    public static GcMetricResult extractResult(Collection<MeterData> meters) {
        long size = findMetersByName(meters, "jvm.gc.live.data.size").findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long max = findMetersByName(meters, "jvm.gc.max.data.size").findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long allocated = findMetersByName(meters, "jvm.gc.memory.allocated").findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long promoted = findMetersByName(meters, "jvm.gc.memory.promoted").findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        List<ActionResult> actions = Stream.of(
                findMetersByName(meters, "jvm.gc.pause"),
                findMetersByName(meters, "jvm.gc.concurrent.phase.time")
        )
                .flatMap(Function.identity())
                .map(m -> new ActionResult(getAction(m), getCause(m), buildPercentileTable(m, meters)))
                .collect(Collectors.toList());
        if (actions.isEmpty()) {
            return null;
        }
        return new GcMetricResult(size, max, allocated, promoted, actions);
    }

    private static String getAction(MeterData m) {
        return m.getTags().get("action");
    }

    private static String getCause(MeterData m) {
        return m.getTags().get("cause");
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class GcMetricResult {
        private Long size;
        private Long maxSize;
        private Long allocated;
        private Long promoted;
        private Collection<ActionResult> actions;

        @lombok.Data
        @lombok.AllArgsConstructor
        public static class ActionResult {
            private String action;
            private String cause;
            private PercentileTable percentileTable;
        }
    }
}
