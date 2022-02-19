package com.github.ricorodriges.metricui.view.micrometer.jvm;

import com.github.ricorodriges.metricui.extractor.micrometer.jvm.ClassLoaderMetricExtractor.ClassLoaderMetricResult;
import com.github.ricorodriges.metricui.model.view.LabelMetricView;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ClassLoaderMetricViews {

    public static LabelMetricView buildView(ClassLoaderMetricResult r) {
        String label = String.format("%d loaded and %d unloaded classes", r.getLoadedClasses(), r.getUnloadedClasses());
        return new LabelMetricView(label);
    }
}
