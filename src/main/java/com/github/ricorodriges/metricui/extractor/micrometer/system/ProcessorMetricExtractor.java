package com.github.ricorodriges.metricui.extractor.micrometer.system;

import com.github.ricorodriges.metricui.model.MeterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;

import java.util.Collection;

import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.anyAsDouble;
import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.anyAsLong;

/**
 * @see io.micrometer.core.instrument.binder.system.ProcessorMetrics
 */
@UtilityClass
public final class ProcessorMetricExtractor {

    public static ProcessorMetricResult extractResult(Collection<MeterData> meters) {
        long cpuCount = anyAsLong(meters, "system.cpu.count").orElse(0L);
        if (cpuCount == 0L) {
            return null;
        }
        double processUsage = anyAsDouble(meters, "process.cpu.usage").orElse(0D);
        double systemUsage = anyAsDouble(meters, "system.cpu.usage").orElse(0D);

        return new ProcessorMetricResult(processUsage, systemUsage, cpuCount);
    }

    @Data
    @AllArgsConstructor
    public static class ProcessorMetricResult {
        private Double processUsage;
        private Double systemUsage;
        private Long cpuCount;
    }
}
