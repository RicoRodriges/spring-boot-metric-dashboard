package com.github.ricorodriges.metrics.view.jvm;

import com.github.ricorodriges.metrics.extractor.jvm.GcMetricExtractor.GcMetricResult;
import com.github.ricorodriges.metrics.extractor.jvm.GcMetricExtractor.GcMetricResult.ActionResult;
import com.github.ricorodriges.metrics.model.Section;
import com.github.ricorodriges.metrics.model.view.LabelMetricView;
import com.github.ricorodriges.metrics.model.view.MetricView;
import com.github.ricorodriges.metrics.model.view.TableMetricView;
import com.github.ricorodriges.metrics.view.MetricViewUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.github.ricorodriges.metrics.view.MetricViewUtils.bytes;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GcMetricViews {

    public static Section.SubSection buildGcSubSection(GcMetricResult result) {
        List<MetricView> views = new ArrayList<>(3);
        views.add(new LabelMetricView("Young heap increases: " + bytes(result.getAllocated())));
        views.add(new LabelMetricView("Old heap increases: " + bytes(result.getPromoted())));

        TableMetricView table = MetricViewUtils.buildPercentileTableView(null, result.getActions(),
                Arrays.asList("Action", "Cause"), r -> Arrays.asList(r.getAction(), r.getCause()),
                Comparator.comparing(ActionResult::getAction)
                        .thenComparing(ActionResult::getCause),
                ActionResult::getPercentileTable);
        views.add(table);
        return new Section.SubSection("Garbage Collector", views);
    }
}
