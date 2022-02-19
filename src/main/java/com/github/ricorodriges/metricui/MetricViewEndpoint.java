package com.github.ricorodriges.metricui;

import com.github.ricorodriges.metricui.model.MeterData;
import com.github.ricorodriges.metricui.model.Section;
import com.github.ricorodriges.metricui.resolver.MetricViewResolver;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.search.MeterNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.cache.CacheManager;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Endpoint(id = "metricView")
class MetricViewEndpoint {

    private static final String CACHE_METRIC_NAME = "cache.size";

    private final MeterRegistry registry;
    private final MetricViewResolver metricViewResolver;
    private final CacheManager cacheManager;
    private final CacheMetricsRegistrar cacheMetricsRegistrar;

    public MetricViewEndpoint(MeterRegistry registry,
                              MetricViewResolver metricViewResolver,
                              CacheManager cacheManager,
                              CacheMetricsRegistrar cacheMetricsRegistrar) {
        this.registry = registry;
        this.metricViewResolver = metricViewResolver;
        this.cacheManager = cacheManager;
        this.cacheMetricsRegistrar = cacheMetricsRegistrar;
    }

    @ReadOperation
    public List<Section> list() {
        registerAbsentCacheMetrics();

        List<MeterData> data = new ArrayList<>();
        collectData(data, this.registry);

        return this.metricViewResolver.resolveViews(data);
    }

    private void collectData(Collection<MeterData> data, MeterRegistry registry) {
        if (registry instanceof CompositeMeterRegistry) {
            ((CompositeMeterRegistry) registry).getRegistries().forEach(member -> collectData(data, member));
        } else {
            for (Meter meter : registry.getMeters()) {
                final String name = meter.getId().getName();
                final String description = meter.getId().getDescription();
                final Map<String, String> tags = meter.getId().getTags().stream().collect(Collectors.toMap(Tag::getKey, Tag::getValue));
                final Meter.Type type = meter.getId().getType();
                final String unit = meter.getId().getBaseUnit();
                final List<MeterData.Measurement> measurements = StreamSupport.stream(meter.measure().spliterator(), false)
                        .map(v -> new MeterData.Measurement(v.getStatistic(), v.getValue()))
                        .collect(Collectors.toList());

                data.add(new MeterData(name, description, Collections.unmodifiableMap(tags), type, unit, Collections.unmodifiableList(measurements)));
            }
        }
    }

    private void registerAbsentCacheMetrics() {
        if (this.cacheManager != null && this.cacheMetricsRegistrar != null) {
            synchronized (MetricViewEndpoint.class) {
                for (String cacheName : cacheManager.getCacheNames()) {
                    try {
                        registry.get(CACHE_METRIC_NAME).tag("cache", cacheName).meter();
                    } catch (MeterNotFoundException ignore) {
                        cacheMetricsRegistrar.bindCacheToRegistry(cacheManager.getCache(cacheName));
                        log.info("'{}' cache metric has registered. Make sure cache statistic is enabled", cacheName);
                    }
                }
            }
        }
    }

}