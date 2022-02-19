package com.github.ricorodriges.metricui.view;

import com.github.ricorodriges.metricui.extractor.PercentileTable;
import com.github.ricorodriges.metricui.model.view.TableMetricView;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public final class ViewUtils {

    public static String bytesToString(Long value) {
        if (value == null) return null;
        if (value <= 1024) {
            return String.format("%d B", value);
        } else if (value <= 1024 * 1024) {
            return String.format("%.2f KB", value / 1024.);
        } else if (value <= 1024 * 1024 * 1024) {
            return String.format("%.2f MB", value / 1024. / 1024.);
        } else {
            return String.format("%.2f GB", value / 1024. / 1024. / 1024.);
        }
    }

    public static String millisToString(long val) {
        long ms = val % 1000;
        long s = (val / 1000) % 60;
        long m = ((long) (val / 1000 / 60.)) % 60;
        long h = ((long) (val / 1000 / 60. / 60.)) % 24;
        long d = (long) (val / 1000 / 60. / 60. / 24.);
        if (d != 0) {
            return String.format("%d d %d h %d min %d sec", d, h, m, s);
        } else if (h != 0) {
            return String.format("%d h %d min %d sec", h, m, s);
        } else if (m != 0) {
            return String.format("%d min %d sec", m, s);
        } else if (s != 0) {
            return String.format("%d sec %d ms", s, ms);
        } else {
            return String.format("%d ms", ms);
        }
    }

    public static String durationToString(Duration duration) {
        if (duration == null) return null;
        return millisToString(TimeUnit.SECONDS.toMillis(duration.getSeconds()) + TimeUnit.NANOSECONDS.toMillis(duration.getNano()));
    }

    public static <R> TableMetricView buildPercentileTableView(String tableTitle,
                                                               List<String> commonHeaders,
                                                               Collection<R> results,
                                                               Comparator<R> comparator,
                                                               Function<R, List<String>> commonValues,
                                                               Function<R, PercentileTable> toTable) {
        List<String> headers = new LinkedList<>(commonHeaders);
        headers.addAll(List.of("Count", "Total (ms)", "Max (ms)"));
        results.stream()
                .map(toTable)
                .map(PercentileTable::getPercentiles)
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .distinct().sorted()
                .map(v -> String.format("P%.1f (ms)", v * 100))
                .forEach(headers::add);


        List<List<Object>> values = results.stream()
                .sorted(comparator)
                .map(r -> {
                    final PercentileTable table = toTable.apply(r);

                    List<Object> value = new ArrayList<>(headers.size());
                    value.addAll(commonValues.apply(r));
                    value.add(table.getCount());
                    value.add(table.getTotal() != null ? table.getTotal().toMillis() : null);
                    value.add(table.getMax() != null ? table.getMax().toMillis() : null);
                    table.getPercentiles().entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(Map.Entry::getValue)
                            .map(Duration::toMillis)
                            .forEach(value::add);
                    return value;
                })
                .collect(Collectors.toList());
        return new TableMetricView(tableTitle, headers, values);
    }
}
