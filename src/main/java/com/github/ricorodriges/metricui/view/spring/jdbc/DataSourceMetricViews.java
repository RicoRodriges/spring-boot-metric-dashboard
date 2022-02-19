package com.github.ricorodriges.metricui.view.spring.jdbc;

import com.github.ricorodriges.metricui.extractor.spring.jdbc.DataSourceMetricExtractor.DataSourceMetricResult;
import com.github.ricorodriges.metricui.model.Color;
import com.github.ricorodriges.metricui.model.Section;
import com.github.ricorodriges.metricui.model.Tip;
import com.github.ricorodriges.metricui.model.view.MetricView;
import com.github.ricorodriges.metricui.model.view.ProgressMetricView;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public final class DataSourceMetricViews {

    public static Section.SubSection buildJDBCSubSection(Collection<DataSourceMetricResult> results) {
        List<MetricView> views = results.stream()
                .sorted(Comparator.comparing(DataSourceMetricResult::getName))
                .map(r -> {
                    long total = r.getIdleConnections() + r.getActiveConnections();
                    return new ProgressMetricView(String.format("'%s' JDBC connections", r.getName()), total,
                            List.of(
                                    new ProgressMetricView.ProgressValue(r.getActiveConnections(), Color.YELLOW, new Tip("Active connections", String.format("%d active connections", r.getActiveConnections()), false)),
                                    new ProgressMetricView.ProgressValue(r.getIdleConnections(), Color.GREEN, new Tip("Idle connections", String.format("%d idle connections", r.getIdleConnections()), false))
                            ),
                            String.format("total connections - %d, min connections - %d, max connections - %d", total, r.getMinConnections(), r.getMaxConnections()));
                })
                .collect(Collectors.toList());
        return new Section.SubSection("JDBC statistic", views);
    }
}
