package com.github.ricorodriges.metrics.model.view;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LabelMetricView implements MetricView {
    private final String type = "label";
    private String label;
    private boolean html;

    public LabelMetricView(String label) {
        this(label, false);
    }
}
