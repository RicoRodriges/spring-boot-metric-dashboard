package com.github.ricorodriges.metricui.extractor.micrometer.jvm;

import com.github.ricorodriges.metricui.model.MeterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;

import java.util.Collection;

import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.anyAsLong;

/**
 * @see io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
 */
@UtilityClass
public final class ClassLoaderMetricExtractor {

    public static ClassLoaderMetricResult extractResult(Collection<MeterData> meters) {
        long loaded = anyAsLong(meters, "jvm.classes.loaded").orElse(0L);
        long unloaded = anyAsLong(meters, "jvm.classes.unloaded").orElse(0L);
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
