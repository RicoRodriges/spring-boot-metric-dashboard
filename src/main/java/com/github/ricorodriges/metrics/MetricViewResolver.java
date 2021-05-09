package com.github.ricorodriges.metrics;

import com.github.ricorodriges.metrics.model.MeterData;
import com.github.ricorodriges.metrics.model.Section;

import java.util.Collection;
import java.util.List;

public interface MetricViewResolver {
    List<Section> resolveViews(Collection<MeterData> meters);
}
