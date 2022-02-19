package com.github.ricorodriges.metricui.view.hikari;

import com.github.ricorodriges.metricui.extractor.PercentileTable;
import com.github.ricorodriges.metricui.extractor.hikari.HikariMetricExtractor.HikariMetricResult;
import com.github.ricorodriges.metricui.model.Color;
import com.github.ricorodriges.metricui.model.Section;
import com.github.ricorodriges.metricui.model.Tip;
import com.github.ricorodriges.metricui.model.view.MetricView;
import com.github.ricorodriges.metricui.model.view.ProgressMetricView;
import com.github.ricorodriges.metricui.model.view.TableMetricView;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metricui.view.ViewUtils.buildPercentileTableView;
import static org.springframework.util.CollectionUtils.isEmpty;

@UtilityClass
public final class HikariMetricViews {

    public static Section.SubSection buildHikariSubSection(Collection<HikariMetricResult> results) {
        if (isEmpty(results)) return null;

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
                List.of(
                        new ProgressMetricView.ProgressValue(r.getActiveConnections(), Color.YELLOW, new Tip("Active connections", String.format("%d active connections", r.getActiveConnections()), false)),
                        new ProgressMetricView.ProgressValue(r.getIdleConnections(), Color.GREEN, new Tip("Idle connections", String.format("%d idle connections", r.getIdleConnections()), false))
                ),
                String.format("total connections - %d, min connections - %d, max connections - %d, pending threads - %d, timeout exceptions - %d", r.getTotalConnections(), r.getMinConnections(), r.getMaxConnections(), r.getPendingThreads(), r.getTimeoutExceptions()));
    }

    private static TableMetricView buildPercentileView(String tableTitle,
                                                       Collection<HikariMetricResult> results,
                                                       Function<HikariMetricResult, PercentileTable> toTable) {
        return buildPercentileTableView(
                tableTitle, List.of("Pool"), results,
                Comparator.comparing(HikariMetricResult::getPool),
                r -> List.of(r.getPool()), toTable
        );
    }
}
