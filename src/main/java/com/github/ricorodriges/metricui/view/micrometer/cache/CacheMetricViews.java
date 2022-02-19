package com.github.ricorodriges.metricui.view.micrometer.cache;

import com.github.ricorodriges.metricui.extractor.micrometer.cache.CacheMetricExtractor.CacheMetricResult;
import com.github.ricorodriges.metricui.model.Section;
import com.github.ricorodriges.metricui.model.view.TableMetricView;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public final class CacheMetricViews {

    private static final List<String> HEADERS = List.of(
            "Name", "Size", "Hits", "Misses", "Puts", "Evictions"
    );
    private static final Function<CacheMetricResult, List<Object>> MAPPER = r -> List.of(
            (Object) r.getCache(), r.getSize(), r.getHits(), r.getMisses(), r.getPuts(), r.getEvictions()
    );

    public static List<Section.SubSection> buildCacheSubSections(Collection<CacheMetricResult> results) {
        return results.stream()
                .map(CacheMetricResult::getCacheManager)
                .distinct().sorted()
                .map(manager -> new Section.SubSection(String.format("'%s' cache manager", manager), List.of(buildCacheTable(results, manager))))
                .collect(Collectors.toList());
    }

    private static TableMetricView buildCacheTable(Collection<CacheMetricResult> results, String cacheManager) {
        List<List<Object>> values = results.stream()
                .filter(r -> cacheManager.equals(r.getCacheManager()))
                .sorted(Comparator.comparing(CacheMetricResult::getCache))
                .map(MAPPER)
                .collect(Collectors.toList());
        return new TableMetricView(HEADERS, values);
    }
}
