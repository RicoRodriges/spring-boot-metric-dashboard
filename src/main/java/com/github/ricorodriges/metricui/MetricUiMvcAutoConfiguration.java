package com.github.ricorodriges.metricui;

import com.github.ricorodriges.metricui.ui.MetricUIMvcController;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration(proxyBeanMethods = false)
@ConditionalOnExpression(MetricUIConfigProperties.ENABLED)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class MetricUiMvcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    MetricUIMvcController metricUIMvcController(MetricUIConfigProperties metricUIConfigProperties,
                                                Optional<ServerProperties> serverProperties,
                                                Optional<ManagementServerProperties> managementServerProperties,
                                                Optional<WebEndpointProperties> webEndpointProperties) {
        return new MetricUIMvcController(metricUIConfigProperties, serverProperties.orElse(null),
                managementServerProperties.orElse(null), webEndpointProperties.orElse(null));
    }
}
