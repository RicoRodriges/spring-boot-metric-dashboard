package com.github.ricorodriges.metricui;

import com.github.ricorodriges.metricui.resolver.DefaultMetricViewResolver;
import com.github.ricorodriges.metricui.resolver.MetricViewResolver;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Timed.class)
@ConditionalOnBean(MeterRegistry.class)
@ConditionalOnAvailableEndpoint(endpoint = MetricViewEndpoint.class)
@AutoConfigureAfter({MetricsAutoConfiguration.class, CompositeMeterRegistryAutoConfiguration.class})
public class MetricViewEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    MetricViewResolver metricViewResolver() {
        return new DefaultMetricViewResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    MetricViewEndpoint metricViewEndpoint(MeterRegistry registry, MetricViewResolver metricViewResolver,
                                          @Autowired(required = false) CacheManager cacheManager,
                                          @Autowired(required = false) CacheMetricsRegistrar cacheMetricsRegistrar) {
        return new MetricViewEndpoint(registry, metricViewResolver, cacheManager, cacheMetricsRegistrar);
    }

    @Bean
    @ConditionalOnMissingBean
    TimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    CountedAspect countedAspect(MeterRegistry meterRegistry) {
        return new CountedAspect(meterRegistry);
    }
}
