package com.github.ricorodriges.metricui.view.micrometer.jvm;

import com.github.ricorodriges.metricui.extractor.micrometer.jvm.ThreadMetricExtractor.ThreadMetricResult;
import com.github.ricorodriges.metricui.model.Color;
import com.github.ricorodriges.metricui.model.Section;
import com.github.ricorodriges.metricui.model.Width;
import com.github.ricorodriges.metricui.model.view.MetricView;
import com.github.ricorodriges.metricui.model.view.ProgressMetricView;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public final class ThreadMetricViews {

    private static final Map<Thread.State, Color> THREAD_TO_COLOR = Map.of(
            Thread.State.NEW, Color.GREEN,
            Thread.State.RUNNABLE, Color.GREEN,
            Thread.State.WAITING, Color.YELLOW,
            Thread.State.TIMED_WAITING, Color.YELLOW,
            Thread.State.BLOCKED, Color.RED,
            Thread.State.TERMINATED, Color.RED
    );

    public static Section.SubSection buildThreadSubSection(ThreadMetricResult result, Width width) {
        List<MetricView> views = new ArrayList<>(1 + Thread.State.values().length);
        views.add(buildDaemonView(result));
        views.addAll(buildThreadStatesView(result));

        String title = String.format("Threads (Total: %d, Peak: %d)", result.getCount(), result.getPeak());
        return new Section.SubSection(title, views, width);
    }

    private static MetricView buildDaemonView(ThreadMetricResult r) {
        String title = String.format("Daemon threads (%d)", r.getDaemons());
        return new ProgressMetricView(title, r.getCount(), r.getDaemons(), null);
    }

    private static List<ProgressMetricView> buildThreadStatesView(ThreadMetricResult result) {
        final long count = result.getCount();
        return result.getThreads().entrySet().stream()
                .map(e -> {
                    Thread.State state = e.getKey();
                    String title = String.format("%s (%d)", state, e.getValue());
                    Color color = THREAD_TO_COLOR.getOrDefault(state, Color.BLUE);
                    return new ProgressMetricView(title, count, new ProgressMetricView.ProgressValue(e.getValue(), color), null);
                })
                .collect(Collectors.toList());
    }
}
