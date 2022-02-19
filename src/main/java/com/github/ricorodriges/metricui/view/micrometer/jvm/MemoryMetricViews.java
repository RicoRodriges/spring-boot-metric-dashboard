package com.github.ricorodriges.metricui.view.micrometer.jvm;

import com.github.ricorodriges.metricui.extractor.micrometer.jvm.MemoryMetricExtractor.MemoryMetricResult;
import com.github.ricorodriges.metricui.extractor.micrometer.jvm.MemoryMetricExtractor.MemoryMetricResult.MemoryAreaResult;
import com.github.ricorodriges.metricui.model.Color;
import com.github.ricorodriges.metricui.model.Section;
import com.github.ricorodriges.metricui.model.Tip;
import com.github.ricorodriges.metricui.model.Width;
import com.github.ricorodriges.metricui.model.view.MetricView;
import com.github.ricorodriges.metricui.model.view.ProgressMetricView;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metricui.view.ViewUtils.bytesToString;

@UtilityClass
public final class MemoryMetricViews {

    public static Section.SubSection buildMemoryPoolsSubSection(MemoryMetricResult r) {
        List<MetricView> views = List.of(
                buildMemoryProgress(r.getHeap(), "Heap"),
                buildMemoryProgress(r.getNonHeap(), "Non-Heap")
        );
        return new Section.SubSection("Memory Pools", views);
    }

    private static ProgressMetricView buildMemoryProgress(Collection<MemoryAreaResult> results, String name) {

        final Integer[] i = {0};
        List<ProgressMetricView.ProgressValue> values = results.stream()
                .sorted(Comparator.comparing(MemoryAreaResult::getName))
                .map(r -> {
                    final Color color = Color.values()[i[0] % Color.values().length];
                    i[0]++;

                    String tipText = String.format("Used: %s / %s", bytesToString(r.getUsed()), bytesToString(r.getCommitted()));
                    tipText = tipText + (r.getLimit() == -1 ? "" : ("<br/>Limit: " + bytesToString(r.getLimit())));
                    Tip hover = new Tip(r.getName(), tipText, true);

                    return new ProgressMetricView.ProgressValue(r.getUsed(), color, hover);
                })
                .collect(Collectors.toList());

        long totalCommitted = results.stream().mapToLong(MemoryAreaResult::getCommitted).sum();
        long totalUsed = results.stream().mapToLong(MemoryAreaResult::getUsed).sum();
        String title = String.format("%s memory (%s / %s)", name, bytesToString(totalUsed), bytesToString(totalCommitted));
        return new ProgressMetricView(title, totalCommitted, values, null);
    }

    public static Section.SubSection buildBufferPoolsSubSection(MemoryMetricResult results, Width w) {
        List<ProgressMetricView> views = results.getBuffers().stream()
                .sorted(Comparator.comparing(MemoryMetricResult.BufferResult::getName))
                .map(r -> {
                    String title = String.format("%s buffer (Total: %d)", r.getName(), r.getCount());
                    String description = String.format("%s / %s", bytesToString(r.getUsed()), bytesToString(r.getCapacity()));
                    return new ProgressMetricView(title, r.getCapacity(), r.getUsed(), description);
                })
                .collect(Collectors.toList());
        return new Section.SubSection("Buffer Pools", views, w);
    }
}
