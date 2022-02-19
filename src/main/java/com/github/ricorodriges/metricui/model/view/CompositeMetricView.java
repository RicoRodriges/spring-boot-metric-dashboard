package com.github.ricorodriges.metricui.model.view;

import com.github.ricorodriges.metricui.model.Width;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CompositeMetricView implements MetricView {
    private final String type = "composite";
    private Width width;
    private String title;
    private List<? extends MetricView> views;

    public CompositeMetricView(List<? extends MetricView> views) {
        this(null, null, views);
    }

    public CompositeMetricView(String title, List<? extends MetricView> views) {
        this(null, title, views);
    }
}
