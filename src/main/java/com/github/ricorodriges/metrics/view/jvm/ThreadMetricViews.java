package com.github.ricorodriges.metrics.view.jvm;

import com.github.ricorodriges.metrics.extractor.jvm.ThreadMetricExtractor.ThreadMetricResult;
import com.github.ricorodriges.metrics.model.Color;
import com.github.ricorodriges.metrics.model.Section;
import com.github.ricorodriges.metrics.model.Width;
import com.github.ricorodriges.metrics.model.view.MetricView;
import com.github.ricorodriges.metrics.model.view.ProgressMetricView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ThreadMetricViews {

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
                    return new ProgressMetricView(title, count, new ProgressMetricView.ProgressValue(e.getValue(), threadColor(state)), null);
                })
                .collect(Collectors.toList());
    }

    private static Color threadColor(Thread.State state) {
        switch (state) {
            case NEW:
            case RUNNABLE:
                return Color.GREEN;
            case WAITING:
            case TIMED_WAITING:
                return Color.YELLOW;
            case BLOCKED:
            case TERMINATED:
                return Color.RED;
            default:
                return Color.BLUE;
        }
    }
}
