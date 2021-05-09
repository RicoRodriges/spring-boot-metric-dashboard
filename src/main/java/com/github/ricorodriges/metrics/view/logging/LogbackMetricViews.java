package com.github.ricorodriges.metrics.view.logging;

import com.github.ricorodriges.metrics.extractor.logging.LogbackMetricExtractor.LogbackMetricResult;
import com.github.ricorodriges.metrics.model.view.TableMetricView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogbackMetricViews {

    public static TableMetricView buildLogbackTableView(LogbackMetricResult result) {
        final Map<String, Long> events = result.getEvents();

        List<String> headers = new ArrayList<>(events.size());
        List<Object> value = new ArrayList<>(events.size());
        events.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    headers.add(StringUtils.capitalize(e.getKey()));
                    value.add(e.getValue());
                });
        return new TableMetricView(headers, Collections.singletonList(value));
    }
}
