package com.github.ricorodriges.metrics.model.view;

import com.github.ricorodriges.metrics.model.Color;
import com.github.ricorodriges.metrics.model.Tip;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class ProgressMetricView implements MetricView {
    private final String type = "progress";
    private String title;
    private double max;
    private List<ProgressValue> values;
    private String description;

    public ProgressMetricView(String title, double max, ProgressValue value, String description) {
        this(title, max, Collections.singletonList(value), description);
    }

    public ProgressMetricView(String title, double max, double value, String description) {
        this(title, max, Collections.singletonList(new ProgressValue(value)), description);
    }

    @Data
    @AllArgsConstructor
    public static class ProgressValue {
        private double value;
        private Color color;
        private Tip hover;

        public ProgressValue(double value) {
            this(value, null, null);
        }

        public ProgressValue(double value, Color color) {
            this(value, color, null);
        }
    }
}
