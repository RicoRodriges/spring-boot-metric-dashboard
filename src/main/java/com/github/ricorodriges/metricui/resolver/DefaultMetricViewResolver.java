package com.github.ricorodriges.metricui.resolver;

import com.github.ricorodriges.metricui.extractor.hibernate.HibernateMetricExtractor;
import com.github.ricorodriges.metricui.extractor.hikari.HikariMetricExtractor;
import com.github.ricorodriges.metricui.extractor.micrometer.TimedMetricExtractor;
import com.github.ricorodriges.metricui.extractor.micrometer.cache.CacheMetricExtractor;
import com.github.ricorodriges.metricui.extractor.micrometer.jvm.GcMetricExtractor;
import com.github.ricorodriges.metricui.extractor.micrometer.jvm.MemoryMetricExtractor;
import com.github.ricorodriges.metricui.extractor.micrometer.jvm.MemoryMetricExtractor.MemoryMetricResult;
import com.github.ricorodriges.metricui.extractor.micrometer.jvm.ThreadMetricExtractor;
import com.github.ricorodriges.metricui.extractor.micrometer.logging.LogbackMetricExtractor;
import com.github.ricorodriges.metricui.extractor.micrometer.system.ProcessorMetricExtractor;
import com.github.ricorodriges.metricui.extractor.micrometer.system.UptimeMetricExtractor;
import com.github.ricorodriges.metricui.extractor.spring.jdbc.DataSourceMetricExtractor;
import com.github.ricorodriges.metricui.extractor.spring.web.WebMvcMetricExtractor;
import com.github.ricorodriges.metricui.model.MeterData;
import com.github.ricorodriges.metricui.model.Section;
import com.github.ricorodriges.metricui.model.Width;
import com.github.ricorodriges.metricui.model.view.MetricView;
import com.github.ricorodriges.metricui.view.hikari.HikariMetricViews;
import com.github.ricorodriges.metricui.view.micrometer.TimedMetricViews;
import com.github.ricorodriges.metricui.view.micrometer.jvm.GcMetricViews;
import com.github.ricorodriges.metricui.view.micrometer.jvm.MemoryMetricViews;
import com.github.ricorodriges.metricui.view.micrometer.jvm.ThreadMetricViews;
import com.github.ricorodriges.metricui.view.spring.jdbc.DataSourceMetricViews;
import com.github.ricorodriges.metricui.view.spring.web.WebMvcMetricViews;
import io.micrometer.core.aop.TimedAspect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ricorodriges.metricui.view.hibernate.HibernateMetricViews.buildHibernateSubSections;
import static com.github.ricorodriges.metricui.view.micrometer.cache.CacheMetricViews.buildCacheSubSections;
import static com.github.ricorodriges.metricui.view.micrometer.logging.LogbackMetricViews.buildLogbackTableView;
import static com.github.ricorodriges.metricui.view.micrometer.system.ProcessorMetricViews.buildProcessorProgressView;
import static com.github.ricorodriges.metricui.view.micrometer.system.UptimeMetricViews.buildUptimeLabel;

public class DefaultMetricViewResolver implements MetricViewResolver {

    @Override
    public List<Section> resolveViews(Collection<MeterData> meters) {
        return Stream.of(
                        buildCommonSection(meters),
                        buildGCSection(meters),
                        buildCacheSection(meters),
                        buildLogSection(meters),
                        buildTimedSection(meters),
                        buildHTTPSection(meters, null),
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
            subSections.add(new Section.SubSection("Logback", List.of(view)));
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

    protected Section buildHTTPSection(Collection<MeterData> metrics, String metricName) {
        List<WebMvcMetricExtractor.WebMvcMetricResult> httpResults = WebMvcMetricExtractor.extractResults(metrics, metricName);

        List<Section.SubSection> subSections = new ArrayList<>(1);
        addIfNotEmpty(subSections, httpResults, WebMvcMetricViews::buildHttpSubSections);

        if (subSections.isEmpty()) {
            return null;
        }
        return new Section("HTTP metrics", subSections);
    }

    protected Section buildDatabaseSection(Collection<MeterData> metrics) {
        List<DataSourceMetricExtractor.DataSourceMetricResult> jdbcResults = DataSourceMetricExtractor.extractResults(metrics);
        List<HikariMetricExtractor.HikariMetricResult> hikariResults = HikariMetricExtractor.extractResults(metrics);
        List<HibernateMetricExtractor.HibernateMetricResult> hibernateResults = HibernateMetricExtractor.extractResults(metrics);

        List<Section.SubSection> subSections = new ArrayList<>();
        addIfNotEmpty(subSections, jdbcResults, DataSourceMetricViews::buildJDBCSubSection);
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
