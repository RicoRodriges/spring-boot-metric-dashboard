package com.github.ricorodriges.metricui.extractor;

import com.github.ricorodriges.metricui.model.MeterData;
import io.micrometer.core.instrument.Statistic;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@UtilityClass
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

    public static Optional<Double> getFirstValueAsDouble(MeterData meter) {
        return Optional.of(meter.getMeasurements().iterator())
                .filter(Iterator::hasNext)
                .map(it -> it.next().getValue());
    }

    public static Optional<Long> getFirstValueAsLong(MeterData meter) {
        return getFirstValueAsDouble(meter).map(Double::longValue);
    }

    public static Optional<Duration> getFirstValueAsDuration(MeterData meter) {
        return getFirstValueAsDouble(meter).map(ExtractorUtils::asDuration);
    }

    public static Optional<ZonedDateTime> getFirstValueAsDate(MeterData meter) {
        return getFirstValueAsLong(meter).map(ExtractorUtils::asDate);
    }

    public static Optional<Double> anyAsDouble(Collection<MeterData> meters, String name) {
        return findMetersByName(meters, name).findAny().flatMap(ExtractorUtils::getFirstValueAsDouble);
    }

    public static Optional<Double> anyAsDouble(Collection<MeterData> meters, String name, String tagName, String tagValue) {
        return findMetersByNameAndTag(meters, name, tagName, tagValue).findAny().flatMap(ExtractorUtils::getFirstValueAsDouble);
    }

    public static Optional<Long> anyAsLong(Collection<MeterData> meters, String name) {
        return anyAsDouble(meters, name).map(Double::longValue);
    }

    public static Optional<Long> anyAsLong(Collection<MeterData> meters, String name, String tagName, String tagValue) {
        return anyAsDouble(meters, name, tagName, tagValue).map(Double::longValue);
    }

    public static Optional<ZonedDateTime> anyAsDate(Collection<MeterData> meters, String name) {
        return anyAsLong(meters, name).map(ExtractorUtils::asDate);
    }

    public static Optional<Duration> anyAsDuration(Collection<MeterData> meters, String name) {
        return anyAsDouble(meters, name).map(ExtractorUtils::asDuration);
    }

    private static Duration asDuration(double v) {
        return Duration.ofMillis((long) (v * 1000));
    }

    private static ZonedDateTime asDate(long v) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(v), ZoneOffset.UTC);
    }

    public static PercentileTable buildPercentileTable(MeterData meter, Collection<MeterData> allMeters) {
        long count = meter.getMeasurements().stream().filter(v -> Statistic.COUNT.equals(v.getType())).findAny()
                .map(MeterData.Measurement::getValue).map(Double::longValue)
                .orElse(0L);
        Duration total = meter.getMeasurements().stream().filter(v -> Statistic.TOTAL_TIME.equals(v.getType())).findAny()
                .map(MeterData.Measurement::getValue).map(ExtractorUtils::asDuration)
                .orElse(null);
        Duration max = meter.getMeasurements().stream().filter(v -> Statistic.MAX.equals(v.getType())).findAny()
                .map(MeterData.Measurement::getValue).map(ExtractorUtils::asDuration)
                .orElse(null);

        PercentileTable percentileTable = new PercentileTable(count, total, max);
        final Map<Double, Duration> percentiles = percentileTable.getPercentiles();

        final Predicate<MeterData> hasSameTags = m -> meter.getTags().entrySet().stream()
                .allMatch(e -> e.getValue().equals(m.getTags().get(e.getKey())));
        findMetersByName(allMeters, meter.getName() + ".percentile")
                .filter(hasSameTags)
                .forEach(m ->
                        getFirstValueAsDouble(m).map(ExtractorUtils::asDuration).ifPresent(v -> percentiles.put(getPhi(m), v))
                );
        return percentileTable;
    }

    public static double getPhi(MeterData m) {
        return Double.parseDouble(m.getTags().get("phi"));
    }
}
