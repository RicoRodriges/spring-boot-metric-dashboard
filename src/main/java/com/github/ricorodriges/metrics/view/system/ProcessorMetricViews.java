package com.github.ricorodriges.metrics.view.system;

import com.github.ricorodriges.metrics.extractor.system.ProcessorMetricExtractor.ProcessorMetricResult;
import com.github.ricorodriges.metrics.model.Color;
import com.github.ricorodriges.metrics.model.Tip;
import com.github.ricorodriges.metrics.model.view.ProgressMetricView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessorMetricViews {

    public static ProgressMetricView buildProcessorProgressView(ProcessorMetricResult r) {
        double otherProcessUsage = Math.max(r.getSystemUsage() - r.getProcessUsage(), 0);
        List<ProgressMetricView.ProgressValue> values = Arrays.asList(
                new ProgressMetricView.ProgressValue(r.getProcessUsage(), Color.LIGHT_BLUE, new Tip("JVM Process", String.format("%.2f %%", r.getProcessUsage() * 100), false)),
                new ProgressMetricView.ProgressValue(otherProcessUsage, Color.BLUE, new Tip("Other OS Processes", String.format("%.2f %%", otherProcessUsage * 100), false))
        );

        String title = String.format("CPU (Total: %d)", r.getCpuCount());
        return new ProgressMetricView(title, 1, values, null);
    }
}
