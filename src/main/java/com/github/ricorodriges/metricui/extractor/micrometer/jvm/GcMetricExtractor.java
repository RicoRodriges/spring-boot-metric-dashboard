package com.github.ricorodriges.metricui.extractor.micrometer.jvm;

import com.github.ricorodriges.metricui.extractor.PercentileTable;
import com.github.ricorodriges.metricui.extractor.micrometer.jvm.GcMetricExtractor.GcMetricResult.ActionResult;
import com.github.ricorodriges.metricui.model.MeterData;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.*;

/**
 * @see io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
 */
@UtilityClass
public final class GcMetricExtractor {

    public static GcMetricResult extractResult(Collection<MeterData> meters) {
        long size = anyAsLong(meters, "jvm.gc.live.data.size").orElse(0L);
        long max = anyAsLong(meters, "jvm.gc.max.data.size").orElse(0L);
        long allocated = anyAsLong(meters, "jvm.gc.memory.allocated").orElse(0L);
        long promoted = anyAsLong(meters, "jvm.gc.memory.promoted").orElse(0L);
        List<ActionResult> actions = Stream.of("jvm.gc.pause", "jvm.gc.concurrent.phase.time")
                .flatMap(name -> findMetersByName(meters, name))
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
