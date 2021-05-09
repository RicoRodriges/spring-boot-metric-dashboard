package com.github.ricorodriges.metrics.extractor.jvm;

import com.github.ricorodriges.metrics.extractor.ExtractorUtils;
import com.github.ricorodriges.metrics.model.MeterData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByName;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassLoaderMetricExtractor {

    public static ClassLoaderMetricResult extractResult(Collection<MeterData> meters) {
        long loaded = findMetersByName(meters, "jvm.classes.loaded").findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long unloaded = findMetersByName(meters, "jvm.classes.unloaded").findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        if (loaded == 0L) {
            return null;
        }
        return new ClassLoaderMetricResult(loaded, unloaded);
    }

    @Data
    @AllArgsConstructor
    public static class ClassLoaderMetricResult {
        private long loadedClasses;
        private long unloadedClasses;
    }
}
