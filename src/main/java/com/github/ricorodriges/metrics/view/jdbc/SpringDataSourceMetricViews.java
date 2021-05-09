package com.github.ricorodriges.metrics.view.jdbc;

import com.github.ricorodriges.metrics.extractor.jdbc.SpringDataSourceMetricExtractor.SpringDataSourceMetricResult;
import com.github.ricorodriges.metrics.model.Color;
import com.github.ricorodriges.metrics.model.Section;
import com.github.ricorodriges.metrics.model.Tip;
import com.github.ricorodriges.metrics.model.view.MetricView;
import com.github.ricorodriges.metrics.model.view.ProgressMetricView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpringDataSourceMetricViews {

    public static Section.SubSection buildJDBCSubSection(Collection<SpringDataSourceMetricResult> results) {
        List<MetricView> views = results.stream()
                .sorted(Comparator.comparing(SpringDataSourceMetricResult::getName))
                .map(r -> {
                    long total = r.getIdleConnections() + r.getActiveConnections();
                    return new ProgressMetricView(String.format("'%s' JDBC connections", r.getName()), total,
                            Arrays.asList(
                                    new ProgressMetricView.ProgressValue(r.getActiveConnections(), Color.YELLOW, new Tip("Active connections", String.format("%d active connections", r.getActiveConnections()), false)),
                                    new ProgressMetricView.ProgressValue(r.getIdleConnections(), Color.GREEN, new Tip("Idle connections", String.format("%d idle connections", r.getIdleConnections()), false))
                            ),
                            String.format("total connections - %d, min connections - %d, max connections - %d", total, r.getMinConnections(), r.getMaxConnections()));
                })
                .collect(Collectors.toList());
        return new Section.SubSection("JDBC statistic", views);
    }
}
