package com.github.ricorodriges.metrics.model.view;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TableMetricView implements MetricView {
    private final String type = "table";
    private String title;
    private List<String> headers;
    private List<List<Object>> values;

    public TableMetricView(List<String> headers, List<List<Object>> values) {
        this(null, headers, values);
    }
}
