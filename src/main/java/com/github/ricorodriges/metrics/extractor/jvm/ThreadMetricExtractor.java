package com.github.ricorodriges.metrics.extractor.jvm;

import com.github.ricorodriges.metrics.extractor.ExtractorUtils;
import com.github.ricorodriges.metrics.model.MeterData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByName;
import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.getFirstValueAsLong;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ThreadMetricExtractor {

    public static ThreadMetricResult extractResult(Collection<MeterData> meters) {
        long threadCount = findMetersByName(meters, "jvm.threads.live").findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        if (threadCount == 0L) {
            return null;
        }
        long threadPeak = findMetersByName(meters, "jvm.threads.peak").findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long daemons = findMetersByName(meters, "jvm.threads.daemon").findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
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
        switch (m.getTags().get("state")) {
            case "new":
                return Thread.State.NEW;
            case "runnable":
                return Thread.State.RUNNABLE;
            case "waiting":
                return Thread.State.WAITING;
            case "timed-waiting":
                return Thread.State.TIMED_WAITING;
            case "blocked":
                return Thread.State.BLOCKED;
            case "terminated":
                return Thread.State.TERMINATED;
            default:
                throw new IllegalStateException();
        }
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
