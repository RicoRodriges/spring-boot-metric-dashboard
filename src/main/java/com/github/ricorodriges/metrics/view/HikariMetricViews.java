package com.github.ricorodriges.metrics.view;

import com.github.ricorodriges.metrics.extractor.HikariMetricExtractor.HikariMetricResult;
import com.github.ricorodriges.metrics.extractor.PercentileTable;
import com.github.ricorodriges.metrics.model.Color;
import com.github.ricorodriges.metrics.model.Section;
import com.github.ricorodriges.metrics.model.Tip;
import com.github.ricorodriges.metrics.model.view.MetricView;
import com.github.ricorodriges.metrics.model.view.ProgressMetricView;
import com.github.ricorodriges.metrics.model.view.TableMetricView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HikariMetricViews {

    public static Section.SubSection buildHikariSubSection(Collection<HikariMetricResult> results) {
        if (results.isEmpty()) return null;

        List<MetricView> views = results.stream()
                .sorted(Comparator.comparing(HikariMetricResult::getPool))
                .map(HikariMetricViews::buildHikariProgressView)
                .collect(Collectors.toList());
        views.add(buildPercentileView("Connection creation time", results, HikariMetricResult::getCreation));
        views.add(buildPercentileView("Connection acquire time", results, HikariMetricResult::getAcquire));
        views.add(buildPercentileView("Connection usage time", results, HikariMetricResult::getUsage));
        return new Section.SubSection("Hikari statistic", views);
    }

    private static ProgressMetricView buildHikariProgressView(HikariMetricResult r) {
        return new ProgressMetricView(String.format("'%s' Hikari connection pool", r.getPool()), r.getTotalConnections(),
                Arrays.asList(
                        new ProgressMetricView.ProgressValue(r.getActiveConnections(), Color.YELLOW, new Tip("Active connections", String.format("%d active connections", r.getActiveConnections()), false)),
                        new ProgressMetricView.ProgressValue(r.getIdleConnections(), Color.GREEN, new Tip("Idle connections", String.format("%d idle connections", r.getIdleConnections()), false))
                ),
                String.format("total connections - %d, min connections - %d, max connections - %d, pending threads - %d, timeout exceptions - %d", r.getTotalConnections(), r.getMinConnections(), r.getMaxConnections(), r.getPendingThreads(), r.getTimeoutExceptions()));
    }

    private static TableMetricView buildPercentileView(String tableTitle,
                                                       Collection<HikariMetricResult> results,
                                                       Function<HikariMetricResult, PercentileTable> toTable) {
        return MetricViewUtils.buildPercentileTableView(tableTitle, results,
                Collections.singletonList("Pool"), r -> Collections.singletonList(r.getPool()),
                Comparator.comparing(HikariMetricResult::getPool), toTable);
    }
}
