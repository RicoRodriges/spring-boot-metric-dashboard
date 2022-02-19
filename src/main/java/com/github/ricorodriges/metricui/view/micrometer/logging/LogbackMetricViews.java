package com.github.ricorodriges.metricui.view.micrometer.logging;

import com.github.ricorodriges.metricui.extractor.micrometer.logging.LogbackMetricExtractor.LogbackMetricResult;
import com.github.ricorodriges.metricui.model.view.TableMetricView;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.capitalize;

@UtilityClass
public final class LogbackMetricViews {

    public static TableMetricView buildLogbackTableView(LogbackMetricResult result) {
        final Map<String, Long> events = result.getEvents();

        List<String> headers = new ArrayList<>(events.size());
        List<Object> value = new ArrayList<>(events.size());
        events.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    headers.add(capitalize(e.getKey()));
                    value.add(e.getValue());
                });
        return new TableMetricView(headers, List.of(value));
    }
}
