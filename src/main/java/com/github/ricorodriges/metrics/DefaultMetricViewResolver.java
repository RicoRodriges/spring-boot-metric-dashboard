package com.github.ricorodriges.metrics;

import com.github.ricorodriges.metrics.extractor.HikariMetricExtractor;
import com.github.ricorodriges.metrics.extractor.TimedMetricExtractor;
import com.github.ricorodriges.metrics.extractor.cache.CacheMetricExtractor;
import com.github.ricorodriges.metrics.extractor.http.SpringMvcMetricExtractor;
import com.github.ricorodriges.metrics.extractor.jdbc.SpringDataSourceMetricExtractor;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor;
import com.github.ricorodriges.metrics.extractor.jvm.GcMetricExtractor;
import com.github.ricorodriges.metrics.extractor.jvm.MemoryMetricExtractor;
import com.github.ricorodriges.metrics.extractor.jvm.MemoryMetricExtractor.MemoryMetricResult;
import com.github.ricorodriges.metrics.extractor.jvm.ThreadMetricExtractor;
import com.github.ricorodriges.metrics.extractor.logging.LogbackMetricExtractor;
import com.github.ricorodriges.metrics.extractor.system.ProcessorMetricExtractor;
import com.github.ricorodriges.metrics.extractor.system.UptimeMetricExtractor;
import com.github.ricorodriges.metrics.model.MeterData;
import com.github.ricorodriges.metrics.model.Section;
import com.github.ricorodriges.metrics.model.Width;
import com.github.ricorodriges.metrics.model.view.MetricView;
import com.github.ricorodriges.metrics.view.HikariMetricViews;
import com.github.ricorodriges.metrics.view.TimedMetricViews;
import com.github.ricorodriges.metrics.view.http.SpringMvcMetricViews;
import com.github.ricorodriges.metrics.view.jdbc.SpringDataSourceMetricViews;
import com.github.ricorodriges.metrics.view.jvm.GcMetricViews;
import com.github.ricorodriges.metrics.view.jvm.MemoryMetricViews;
import com.github.ricorodriges.metrics.view.jvm.ThreadMetricViews;
import io.micrometer.core.aop.TimedAspect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ricorodriges.metrics.view.cache.CacheMetricViews.buildCacheSubSections;
import static com.github.ricorodriges.metrics.view.jpa.HibernateMetricViews.buildHibernateSubSections;
import static com.github.ricorodriges.metrics.view.logging.LogbackMetricViews.buildLogbackTableView;
import static com.github.ricorodriges.metrics.view.system.ProcessorMetricViews.buildProcessorProgressView;
import static com.github.ricorodriges.metrics.view.system.UptimeMetricViews.buildUptimeLabel;

public class DefaultMetricViewResolver implements MetricViewResolver {

    @Override
    public List<Section> resolveViews(Collection<MeterData> meters) {
        return Stream.of(
                buildCommonSection(meters),
                buildGCSection(meters),
                buildCacheSection(meters),
                buildLogSection(meters),
                buildTimedSection(meters),
                buildHTTPSection(meters),
                buildDatabaseSection(meters)
        )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected boolean isTimedMetric(MeterData meter) {
        return TimedAspect.DEFAULT_METRIC_NAME.equals(meter.getName()) ||
                "method.counted".equals(meter.getName());
    }

    protected Section buildCommonSection(Collection<MeterData> meters) {
        MemoryMetricResult memoryMetricResult = MemoryMetricExtractor.extractResult(meters);
        ThreadMetricExtractor.ThreadMetricResult threadMetricResult = ThreadMetricExtractor.extractResult(meters);
        ProcessorMetricExtractor.ProcessorMetricResult processorMetricResult = ProcessorMetricExtractor.extractResult(meters);
        UptimeMetricExtractor.UptimeMetricResult uptimeMetricResult = UptimeMetricExtractor.extractResult(meters);

        List<Section.SubSection> subSections = new ArrayList<>(4);
        if (processorMetricResult != null || uptimeMetricResult != null) {
            List<MetricView> views = Stream.of(
                    processorMetricResult != null ? buildProcessorProgressView(processorMetricResult) : null,
                    uptimeMetricResult != null ? buildUptimeLabel(uptimeMetricResult) : null
            )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            subSections.add(new Section.SubSection("System", views, Width.THIRD));
        }
        addIfNotEmpty(subSections, threadMetricResult, Width.THIRD, ThreadMetricViews::buildThreadSubSection);
        addIfNotEmpty(subSections, memoryMetricResult, Width.THIRD, MemoryMetricViews::buildBufferPoolsSubSection);
        addIfNotEmpty(subSections, memoryMetricResult, MemoryMetricViews::buildMemoryPoolsSubSection);
        if (subSections.isEmpty()) {
            return null;
        }
        return new Section("Common metrics", subSections);
    }

    protected Section buildGCSection(Collection<MeterData> metrics) {
        GcMetricExtractor.GcMetricResult gcResult = GcMetricExtractor.extractResult(metrics);

        List<Section.SubSection> subSections = new ArrayList<>(1);
        addIfNotEmpty(subSections, gcResult, GcMetricViews::buildGcSubSection);

        if (subSections.isEmpty()) {
            return null;
        }
        return new Section("GC metrics", subSections);
    }

    protected Section buildCacheSection(Collection<MeterData> meters) {
        List<CacheMetricExtractor.CacheMetricResult> cacheResults = CacheMetricExtractor.extractResults(meters);

        List<Section.SubSection> subSections = new ArrayList<>();
        subSections.addAll(buildCacheSubSections(cacheResults));

        if (subSections.isEmpty()) {
            return null;
        }
        return new Section("Cache metrics", subSections);
    }

    protected Section buildLogSection(Collection<MeterData> meters) {
        LogbackMetricExtractor.LogbackMetricResult logbackResult = LogbackMetricExtractor.extractResult(meters);

        List<Section.SubSection> subSections = new ArrayList<>(1);
        if (logbackResult != null) {
            MetricView view = buildLogbackTableView(logbackResult);
            subSections.add(new Section.SubSection("Logback", Collections.singletonList(view)));
        }

        if (subSections.isEmpty()) {
            return null;
        }
        return new Section("Log metrics", subSections);
    }

    protected Section buildTimedSection(Collection<MeterData> metrics) {
        List<TimedMetricExtractor.TimedMetricResult> timedResult = TimedMetricExtractor.extractResults(metrics, this::isTimedMetric);

        List<Section.SubSection> subSections = new ArrayList<>(1);
        addIfNotEmpty(subSections, timedResult, TimedMetricViews::buildTimedSubSection);

        if (subSections.isEmpty()) {
            return null;
        }
        return new Section("Timed metrics", subSections);
    }

    protected Section buildHTTPSection(Collection<MeterData> metrics) {
        List<SpringMvcMetricExtractor.SpringMvcMetricResult> httpResults = SpringMvcMetricExtractor.extractResults(metrics);

        List<Section.SubSection> subSections = new ArrayList<>(1);
        addIfNotEmpty(subSections, httpResults, SpringMvcMetricViews::buildHttpSubSections);

        if (subSections.isEmpty()) {
            return null;
        }
        return new Section("HTTP metrics", subSections);
    }

    protected Section buildDatabaseSection(Collection<MeterData> metrics) {
        List<SpringDataSourceMetricExtractor.SpringDataSourceMetricResult> jdbcResults = SpringDataSourceMetricExtractor.extractResults(metrics);
        List<HikariMetricExtractor.HikariMetricResult> hikariResults = HikariMetricExtractor.extractResults(metrics);
        List<HibernateMetricExtractor.HibernateMetricResult> hibernateResults = HibernateMetricExtractor.extractResults(metrics);

        List<Section.SubSection> subSections = new ArrayList<>();
        addIfNotEmpty(subSections, jdbcResults, SpringDataSourceMetricViews::buildJDBCSubSection);
        addIfNotEmpty(subSections, hikariResults, HikariMetricViews::buildHikariSubSection);
        subSections.addAll(buildHibernateSubSections(hibernateResults));

        if (subSections.isEmpty()) {
            return null;
        }
        return new Section("Database metrics", subSections);
    }

    protected static <C, R> void addIfNotEmpty(Collection<C> collection, R element, Function<R, C> mapper) {
        if (element != null) {
            C newElement = mapper.apply(element);
            if (newElement instanceof Collection && !((Collection<?>) newElement).isEmpty()) {
                collection.add(newElement);
            } else if (newElement != null) {
                collection.add(newElement);
            }
        }
    }

    protected static <C, R> void addIfNotEmpty(Collection<C> collection, R element, Width w,
                                               BiFunction<R, Width, C> mapper) {
        addIfNotEmpty(collection, element, v -> mapper.apply(v, w));
    }
}
