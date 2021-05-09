package com.github.ricorodriges.metrics.view.jvm;

import com.github.ricorodriges.metrics.extractor.jvm.ClassLoaderMetricExtractor.ClassLoaderMetricResult;
import com.github.ricorodriges.metrics.model.view.LabelMetricView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassLoaderMetricViews {

    public static LabelMetricView buildView(ClassLoaderMetricResult r) {
        String label = String.format("%d loaded and %d unloaded classes", r.getLoadedClasses(), r.getUnloadedClasses());
        return new LabelMetricView(label);
    }
}
