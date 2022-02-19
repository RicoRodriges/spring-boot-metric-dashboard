package com.github.ricorodriges.metricui.resolver;

import com.github.ricorodriges.metricui.model.MeterData;
import com.github.ricorodriges.metricui.model.Section;

import java.util.Collection;
import java.util.List;

public interface MetricViewResolver {
    List<Section> resolveViews(Collection<MeterData> meters);
}
