package com.github.ricorodriges.metricui.extractor.micrometer.jvm;

import com.github.ricorodriges.metricui.extractor.micrometer.jvm.MemoryMetricExtractor.MemoryMetricResult.BufferResult;
import com.github.ricorodriges.metricui.extractor.micrometer.jvm.MemoryMetricExtractor.MemoryMetricResult.MemoryAreaResult;
import com.github.ricorodriges.metricui.model.MeterData;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.anyAsLong;
import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.findMetersByName;

/**
 * @see io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
 */
@UtilityClass
public final class MemoryMetricExtractor {

    public static MemoryMetricResult extractResult(Collection<MeterData> meters) {
        Collection<BufferResult> buffers = extractBuffers(meters);
        if (buffers.isEmpty()) {
            return null;
        }
        Collection<MemoryAreaResult> heap = extractMemoryArea(meters, "heap");
        Collection<MemoryAreaResult> nonHeap = extractMemoryArea(meters, "nonheap");
        return new MemoryMetricResult(buffers, heap, nonHeap);
    }

    private static Collection<MemoryAreaResult> extractMemoryArea(Collection<MeterData> meters, String tagArea) {
        final List<MeterData> memoryMeters = meters.stream()
                .filter(m -> m.getName().startsWith("jvm.memory."))
                .filter(m -> tagArea.equals(m.getTags().get("area")))
                .collect(Collectors.toList());
        return findMetersByName(memoryMeters, "jvm.memory.committed")
                .map(m -> m.getTags().get("id"))
                .distinct()
                .map(memoryId -> {
                    long commited = anyAsLong(memoryMeters, "jvm.memory.committed", "id", memoryId).orElse(0L);
                    long used = anyAsLong(memoryMeters, "jvm.memory.used", "id", memoryId).orElse(0L);
                    long limit = anyAsLong(memoryMeters, "jvm.memory.max", "id", memoryId).orElse(-1L);
                    return new MemoryAreaResult(memoryId, commited, used, limit);
                })
                .collect(Collectors.toList());
    }

    private static Collection<BufferResult> extractBuffers(Collection<MeterData> meters) {
        final List<MeterData> bufferMeters = meters.stream()
                .filter(m -> m.getName().startsWith("jvm.buffer."))
                .collect(Collectors.toList());
        return findMetersByName(bufferMeters, "jvm.buffer.count")
                .map(m -> m.getTags().get("id"))
                .distinct()
                .map(bufferId -> {
                    long count = anyAsLong(bufferMeters, "jvm.buffer.count", "id", bufferId).orElse(0L);
                    long used = anyAsLong(bufferMeters, "jvm.buffer.memory.used", "id", bufferId).orElse(0L);
                    long capacity = anyAsLong(bufferMeters, "jvm.buffer.total.capacity", "id", bufferId).orElse(0L);
                    return new BufferResult(bufferId, count, used, capacity);
                })
                .collect(Collectors.toList());
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MemoryMetricResult {
        private Collection<BufferResult> buffers;

        private Collection<MemoryAreaResult> heap;
        private Collection<MemoryAreaResult> nonHeap;

        @lombok.Data
        @lombok.AllArgsConstructor
        public static class BufferResult {
            private String name;
            private Long count;
            private Long used;
            private Long capacity;
        }

        @lombok.Data
        @lombok.AllArgsConstructor
        public static class MemoryAreaResult {
            private String name;
            private Long committed;
            private Long used;
            private Long limit;
        }
    }
}
