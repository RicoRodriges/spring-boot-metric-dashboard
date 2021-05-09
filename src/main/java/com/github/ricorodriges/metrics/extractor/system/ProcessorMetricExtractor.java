package com.github.ricorodriges.metrics.extractor.system;

import com.github.ricorodriges.metrics.extractor.ExtractorUtils;
import com.github.ricorodriges.metrics.model.MeterData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByName;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessorMetricExtractor {

    public static ProcessorMetricResult extractResult(Collection<MeterData> meters) {
        long cpuCount = findMetersByName(meters, "system.cpu.count").findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        if (cpuCount == 0L) {
            return null;
        }
        double processUsage = findMetersByName(meters, "process.cpu.usage").findAny()
                .flatMap(ExtractorUtils::getFirstValue)
                .orElse(0D);
        double systemUsage = findMetersByName(meters, "system.cpu.usage").findAny()
                .flatMap(ExtractorUtils::getFirstValue)
                .orElse(0D);

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
