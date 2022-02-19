package com.github.ricorodriges.metricui;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "metricui")
public class MetricUIConfigProperties {

    public static final String ENABLED = "${metricui.enabled:false}";
    public static final String METRIC_UI_PATH = "${metricui.path:#{'/metrics'}}";
    public static final String ACTUATOR_PATH_PLACEHOLDER = "metricui.actuator-path";

    /**
     * Enable UI endpoint.
     */
    private boolean enabled = false;

    /**
     * UI endpoint path.
     */
    private String path = "/metrics";

    /**
     * Actuator relative or absolute path.
     */
    private String actuatorPath;
}
