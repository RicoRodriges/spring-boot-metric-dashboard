package com.github.ricorodriges.metrics.view;

import com.github.ricorodriges.metrics.extractor.TimedMetricExtractor.TimedMetricResult;
import com.github.ricorodriges.metrics.model.Section;
import com.github.ricorodriges.metrics.model.view.TableMetricView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimedMetricViews {

    public static Section.SubSection buildTimedSubSection(Collection<TimedMetricResult> results) {
        if (results.isEmpty()) return null;

        TableMetricView view = MetricViewUtils.buildPercentileTableView(null, results,
                Arrays.asList("Name", "Exception"),
                r -> Arrays.asList(String.format("%s.%s", r.getClassName(), r.getMethod()), r.getException()),
                Comparator.comparing(TimedMetricResult::getClassName)
                        .thenComparing(TimedMetricResult::getMethod)
                        .thenComparing(TimedMetricResult::getException),
                TimedMetricResult::getPercentileTable);
        return new Section.SubSection("Timed statistic", Collections.singletonList(view));
    }
}
