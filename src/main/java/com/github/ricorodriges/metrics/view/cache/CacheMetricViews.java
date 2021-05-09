package com.github.ricorodriges.metrics.view.cache;

import com.github.ricorodriges.metrics.extractor.cache.CacheMetricExtractor.CacheMetricResult;
import com.github.ricorodriges.metrics.model.Section;
import com.github.ricorodriges.metrics.model.view.TableMetricView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheMetricViews {

    private static final List<String> HEADERS = Collections.unmodifiableList(
            Arrays.asList("Name", "Size", "Hits", "Misses", "Puts", "Evictions"));

    public static List<Section.SubSection> buildCacheSubSections(Collection<CacheMetricResult> results) {
        return results.stream()
                .map(CacheMetricResult::getCacheManager)
                .distinct().sorted()
                .map(manager -> new Section.SubSection(String.format("'%s' cache manager", manager), Collections.singletonList(buildCacheTable(results, manager))))
                .collect(Collectors.toList());
    }

    private static TableMetricView buildCacheTable(Collection<CacheMetricResult> results, String cacheManager) {
        List<List<Object>> values = results.stream()
                .filter(r -> cacheManager.equals(r.getCacheManager()))
                .sorted(Comparator.comparing(CacheMetricResult::getCache))
                .map(r -> Arrays.asList((Object) r.getCache(), r.getSize(),
                        r.getHits(), r.getMisses(),
                        r.getPuts(), r.getEvictions()))
                .collect(Collectors.toList());
        return new TableMetricView(HEADERS, values);
    }
}
