package com.github.ricorodriges.metricui.view.micrometer.system;

import com.github.ricorodriges.metricui.extractor.micrometer.system.ProcessorMetricExtractor.ProcessorMetricResult;
import com.github.ricorodriges.metricui.model.Color;
import com.github.ricorodriges.metricui.model.Tip;
import com.github.ricorodriges.metricui.model.view.ProgressMetricView;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public final class ProcessorMetricViews {

    public static ProgressMetricView buildProcessorProgressView(ProcessorMetricResult r) {
        double otherProcessUsage = Math.max(r.getSystemUsage() - r.getProcessUsage(), 0);
        List<ProgressMetricView.ProgressValue> values = List.of(
                new ProgressMetricView.ProgressValue(r.getProcessUsage(), Color.LIGHT_BLUE, new Tip("JVM Process", String.format("%.2f %%", r.getProcessUsage() * 100), false)),
                new ProgressMetricView.ProgressValue(otherProcessUsage, Color.BLUE, new Tip("Other OS Processes", String.format("%.2f %%", otherProcessUsage * 100), false))
        );

        String title = String.format("CPU (Total: %d)", r.getCpuCount());
        return new ProgressMetricView(title, 1, values, null);
    }
}
