package com.github.ricorodriges.metricui.view.micrometer.jvm;

import com.github.ricorodriges.metricui.extractor.micrometer.jvm.GcMetricExtractor.GcMetricResult;
import com.github.ricorodriges.metricui.extractor.micrometer.jvm.GcMetricExtractor.GcMetricResult.ActionResult;
import com.github.ricorodriges.metricui.model.Section;
import com.github.ricorodriges.metricui.model.view.LabelMetricView;
import com.github.ricorodriges.metricui.model.view.MetricView;
import com.github.ricorodriges.metricui.model.view.TableMetricView;
import lombok.experimental.UtilityClass;

import java.util.Comparator;
import java.util.List;

import static com.github.ricorodriges.metricui.view.ViewUtils.buildPercentileTableView;
import static com.github.ricorodriges.metricui.view.ViewUtils.bytesToString;

@UtilityClass
public final class GcMetricViews {

    public static Section.SubSection buildGcSubSection(GcMetricResult result) {
        TableMetricView table = buildPercentileTableView(null, List.of("Action", "Cause"),
                result.getActions(),
                Comparator.comparing(ActionResult::getAction).thenComparing(ActionResult::getCause),
                r -> List.of(r.getAction(), r.getCause()), ActionResult::getPercentileTable);

        List<MetricView> views = List.of(
                new LabelMetricView("Young heap increases: " + bytesToString(result.getAllocated())),
                new LabelMetricView("Old heap increases: " + bytesToString(result.getPromoted())),
                table
        );
        return new Section.SubSection("Garbage Collector", views);
    }
}
