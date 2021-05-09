package com.github.ricorodriges.metrics.extractor.jvm;

import com.github.ricorodriges.metrics.extractor.ExtractorUtils;
import com.github.ricorodriges.metrics.extractor.jvm.MemoryMetricExtractor.MemoryMetricResult.BufferResult;
import com.github.ricorodriges.metrics.extractor.jvm.MemoryMetricExtractor.MemoryMetricResult.MemoryAreaResult;
import com.github.ricorodriges.metrics.model.MeterData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByName;
import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByNameAndTag;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
        List<MeterData> committed = findMetersByNameAndTag(meters, "jvm.memory.committed", "area", tagArea)
                .collect(Collectors.toList());
        List<MeterData> max = findMetersByNameAndTag(meters, "jvm.memory.max", "area", tagArea)
                .collect(Collectors.toList());
        List<MeterData> used = findMetersByNameAndTag(meters, "jvm.memory.used", "area", tagArea)
                .collect(Collectors.toList());

        return committed.stream()
                .map(MemoryMetricExtractor::getMemoryId)
                .map(id -> {
                    long c = committed.stream().filter(m -> id.equals(getMemoryId(m))).findAny()
                            .flatMap(ExtractorUtils::getFirstValueAsLong)
                            .orElse(0L);
                    long u = used.stream().filter(m -> id.equals(getMemoryId(m))).findAny()
                            .flatMap(ExtractorUtils::getFirstValueAsLong)
                            .orElse(0L);
                    long limit = max.stream().filter(m -> id.equals(getMemoryId(m))).findAny()
                            .flatMap(ExtractorUtils::getFirstValueAsLong)
                            .orElse(-1L);
                    return new MemoryAreaResult(id, c, u, limit);
                })
                .collect(Collectors.toList());
    }

    private static String getMemoryId(MeterData m) {
        return m.getTags().get("id");
    }

    private static Collection<BufferResult> extractBuffers(Collection<MeterData> meters) {
        final List<MeterData> bufferMeters = meters.stream()
                .filter(m -> m.getName().startsWith("jvm.buffer."))
                .collect(Collectors.toList());
        return findMetersByName(bufferMeters, "jvm.buffer.count")
                .map(m -> m.getTags().get("id"))
                .distinct()
                .map(bufferId -> {
                    long count = findMetersByNameAndTag(bufferMeters, "jvm.buffer.count", "id", bufferId).findAny()
                            .flatMap(ExtractorUtils::getFirstValueAsLong)
                            .orElse(0L);
                    long used = findMetersByNameAndTag(bufferMeters, "jvm.buffer.memory.used", "id", bufferId).findAny()
                            .flatMap(ExtractorUtils::getFirstValueAsLong)
                            .orElse(0L);
                    long capacity = findMetersByNameAndTag(bufferMeters, "jvm.buffer.total.capacity", "id", bufferId).findAny()
                            .flatMap(ExtractorUtils::getFirstValueAsLong)
                            .orElse(0L);
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
            private Long max;
        }
    }
}
