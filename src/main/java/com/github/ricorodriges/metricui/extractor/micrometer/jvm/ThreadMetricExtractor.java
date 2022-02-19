package com.github.ricorodriges.metricui.extractor.micrometer.jvm;

import com.github.ricorodriges.metricui.model.MeterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.*;

/**
 * @see io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
 */
@UtilityClass
public final class ThreadMetricExtractor {

    private static final Map<String, Thread.State> STATE_MAPPER = Map.of(
            "new", Thread.State.NEW,
            "runnable", Thread.State.RUNNABLE,
            "waiting", Thread.State.WAITING,
            "timed-waiting", Thread.State.TIMED_WAITING,
            "blocked", Thread.State.BLOCKED,
            "terminated", Thread.State.TERMINATED
    );

    public static ThreadMetricResult extractResult(Collection<MeterData> meters) {
        long threadCount = anyAsLong(meters, "jvm.threads.live").orElse(0L);
        if (threadCount == 0L) {
            return null;
        }
        long threadPeak = anyAsLong(meters, "jvm.threads.peak").orElse(0L);
        long daemons = anyAsLong(meters, "jvm.threads.daemon").orElse(0L);
        Map<Thread.State, Long> threads = extractThreads(meters);
        return new ThreadMetricResult(threadCount, threadPeak, daemons, threads);
    }

    private static Map<Thread.State, Long> extractThreads(Collection<MeterData> meters) {
        final Map<Thread.State, Long> threads = new EnumMap<>(Thread.State.class);
        findMetersByName(meters, "jvm.threads.states")
                .forEach(m -> threads.put(getThreadState(m), getFirstValueAsLong(m).orElse(0L)));
        return threads;
    }

    private static Thread.State getThreadState(MeterData m) {
        String stateName = m.getTags().get("state");
        Thread.State state = STATE_MAPPER.get(stateName);
        if (state == null) {
            throw new IllegalStateException(String.format("Thread state %s is not supported", stateName));
        }
        return state;
    }

    @Data
    @AllArgsConstructor
    public static class ThreadMetricResult {
        private Long count;
        private Long peak;

        private Long daemons;
        private Map<Thread.State, Long> threads;
    }
}
