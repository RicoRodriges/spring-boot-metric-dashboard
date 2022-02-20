package com.example.demo.config;

import com.github.ricorodriges.metricui.model.MeterData;
import com.github.ricorodriges.metricui.model.Section;
import com.github.ricorodriges.metricui.model.view.TableMetricView;
import com.github.ricorodriges.metricui.resolver.DefaultMetricViewResolver;
import com.github.ricorodriges.metricui.resolver.MetricViewResolver;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
public class MetricsConfig {

    @Bean
    @ConditionalOnProperty(name = "metrics.enabled")
    public MetricViewResolver metricViewResolver() {
        return new DefaultMetricViewResolver() {
            @Override
            public List<Section> resolveViews(Collection<MeterData> meters) {
                List<Section> result = new ArrayList<>(super.resolveViews(meters));

                List<List<String>> values = meters.stream()
                        .sorted(Comparator.comparing(MeterData::getName))
                        .map(m -> List.of(
                                m.getName(),
                                m.getTags().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).sorted().collect(Collectors.joining(";")),
                                m.getMeasurements().stream().map(v -> String.valueOf(v.getValue())).collect(Collectors.joining(", ")),
                                m.getUnit() != null ? m.getUnit() : ""
                        ))
                        .collect(Collectors.toList());
                TableMetricView table = new TableMetricView(List.of("Name", "Tags", "Values", "Unit"), (List) values);
                result.add(new Section("All meters", List.of(new Section.SubSection("Meters", List.of(table)))));
                return result;
            }
        };
    }

    @Bean
    @ConditionalOnProperty(name = "metrics.enabled", havingValue = "false")
    public MeterRegistry meterRegistry() {
        // disable all metrics
        SimpleMeterRegistry closedRegistry = new SimpleMeterRegistry();
        closedRegistry.close();
        return closedRegistry;
    }
}
