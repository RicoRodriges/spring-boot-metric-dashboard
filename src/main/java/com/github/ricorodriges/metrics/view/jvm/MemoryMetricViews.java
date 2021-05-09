package com.github.ricorodriges.metrics.view.jvm;

import com.github.ricorodriges.metrics.extractor.jvm.MemoryMetricExtractor.MemoryMetricResult;
import com.github.ricorodriges.metrics.extractor.jvm.MemoryMetricExtractor.MemoryMetricResult.MemoryAreaResult;
import com.github.ricorodriges.metrics.model.Color;
import com.github.ricorodriges.metrics.model.Section;
import com.github.ricorodriges.metrics.model.Tip;
import com.github.ricorodriges.metrics.model.Width;
import com.github.ricorodriges.metrics.model.view.MetricView;
import com.github.ricorodriges.metrics.model.view.ProgressMetricView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metrics.view.MetricViewUtils.bytes;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemoryMetricViews {

    public static Section.SubSection buildMemoryPoolsSubSection(MemoryMetricResult r) {
        List<MetricView> views = new ArrayList<>(2);
        views.add(buildMemoryProgress(r.getHeap(), "Heap"));
        views.add(buildMemoryProgress(r.getNonHeap(), "Non-Heap"));
        return new Section.SubSection("Memory Pools", views);
    }

    private static ProgressMetricView buildMemoryProgress(Collection<MemoryAreaResult> results, String name) {

        final Integer[] i = {0};
        List<ProgressMetricView.ProgressValue> values = results.stream()
                .sorted(Comparator.comparing(MemoryAreaResult::getName))
                .map(r -> {
                    final Color color = Color.values()[i[0] % Color.values().length];
                    i[0]++;

                    String tipText = String.format("Used: %s / %s", bytes(r.getUsed()), bytes(r.getCommitted()));
                    tipText = tipText + (r.getMax() == -1 ? "" : ("<br/>Limit: " + bytes(r.getMax())));
                    Tip hover = new Tip(r.getName(), tipText, true);

                    return new ProgressMetricView.ProgressValue(r.getUsed(), color, hover);
                })
                .collect(Collectors.toList());

        long totalCommitted = results.stream().mapToLong(MemoryAreaResult::getCommitted).sum();
        long totalUsed = results.stream().mapToLong(MemoryAreaResult::getUsed).sum();
        String title = String.format("%s memory (%s / %s)", name, bytes(totalUsed), bytes(totalCommitted));
        return new ProgressMetricView(title, totalCommitted, values, null);
    }

    public static Section.SubSection buildBufferPoolsSubSection(MemoryMetricResult results, Width w) {
        List<ProgressMetricView> views = results.getBuffers().stream()
                .sorted(Comparator.comparing(MemoryMetricResult.BufferResult::getName))
                .map(r -> {
                    String title = String.format("%s buffer (Total: %d)", r.getName(), r.getCount());
                    String description = String.format("%s / %s", bytes(r.getUsed()), bytes(r.getCapacity()));
                    return new ProgressMetricView(title, r.getCapacity(), r.getUsed(), description);
                })
                .collect(Collectors.toList());
        return new Section.SubSection("Buffer Pools", views, w);
    }
}
