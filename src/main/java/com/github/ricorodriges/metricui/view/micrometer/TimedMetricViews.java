package com.github.ricorodriges.metricui.view.micrometer;

import com.github.ricorodriges.metricui.extractor.micrometer.TimedMetricExtractor.TimedMetricResult;
import com.github.ricorodriges.metricui.model.Section;
import com.github.ricorodriges.metricui.model.view.TableMetricView;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static com.github.ricorodriges.metricui.view.ViewUtils.buildPercentileTableView;
import static org.springframework.util.CollectionUtils.isEmpty;

@UtilityClass
public final class TimedMetricViews {

    public static Section.SubSection buildTimedSubSection(Collection<TimedMetricResult> results) {
        if (isEmpty(results)) return null;

        TableMetricView view = buildPercentileTableView(null, List.of("Name", "Exception"),
                results,
                Comparator.comparing(TimedMetricResult::getClassName)
                        .thenComparing(TimedMetricResult::getMethod)
                        .thenComparing(TimedMetricResult::getException),
                r -> List.of(String.format("%s#%s", r.getClassName(), r.getMethod()), r.getException()),
                TimedMetricResult::getPercentileTable);
        return new Section.SubSection("Timed statistic", List.of(view));
    }
}
