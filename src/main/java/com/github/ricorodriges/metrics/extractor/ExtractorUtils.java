package com.github.ricorodriges.metrics.extractor;

import com.github.ricorodriges.metrics.model.MeterData;
import io.micrometer.core.instrument.Statistic;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExtractorUtils {
    public static Stream<MeterData> findMetersByName(Collection<MeterData> meters, String name) {
        return meters.stream()
                .filter(m -> name.equals(m.getName()));
    }

    public static Stream<MeterData> findMetersByNameAndTag(Collection<MeterData> metrics, String name,
                                                           String tagName, String tagValue) {
        return findMetersByName(metrics, name)
                .filter(m -> tagValue.equals(m.getTags().get(tagName)));
    }

    public static Optional<Double> getFirstValue(MeterData meter) {
        return Optional.of(meter.getMeasurements().iterator())
                .filter(Iterator::hasNext)
                .map(it -> it.next().getValue());
    }

    public static Optional<Long> getFirstValueAsLong(MeterData meter) {
        return getFirstValue(meter).map(Double::longValue);
    }

    public static PercentileTable buildPercentileTable(MeterData meter, Collection<MeterData> allMeters) {
        long count = meter.getMeasurements().stream().filter(v -> Statistic.COUNT.equals(v.getType())).findAny()
                .map(MeterData.Measurement::getValue).map(Double::longValue)
                .orElse(0L);
        Duration total = meter.getMeasurements().stream().filter(v -> Statistic.TOTAL_TIME.equals(v.getType())).findAny()
                .map(v -> Duration.ofMillis((long) (v.getValue() * 1000)))
                .orElse(null);
        Duration max = meter.getMeasurements().stream().filter(v -> Statistic.MAX.equals(v.getType())).findAny()
                .map(v -> Duration.ofMillis((long) (v.getValue() * 1000)))
                .orElse(null);

        PercentileTable percentileTable = new PercentileTable(count, total, max);
        final Map<Double, Duration> percentiles = percentileTable.getPercentiles();

        final String percentileMeterName = meter.getName() + ".percentile";
        final Predicate<MeterData> hasSameTags = m -> meter.getTags().entrySet().stream()
                .allMatch(e -> e.getValue().equals(m.getTags().get(e.getKey())));
        allMeters.stream()
                .filter(m -> percentileMeterName.equals(m.getName()))
                .filter(hasSameTags)
                .forEach(m ->
                        getFirstValue(m).ifPresent(v -> percentiles.put(getPhi(m), Duration.ofMillis((long) (v * 1000))))
                );
        return percentileTable;
    }

    public static double getPhi(MeterData m) {
        return Double.parseDouble(m.getTags().get("phi"));
    }
}
