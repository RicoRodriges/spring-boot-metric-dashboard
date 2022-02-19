package com.github.ricorodriges.metricui.ui;

import com.github.ricorodriges.metricui.MetricUIConfigProperties;
import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.ricorodriges.metricui.MetricUIConfigProperties.ACTUATOR_PATH_PLACEHOLDER;
import static com.github.ricorodriges.metricui.MetricUIConfigProperties.METRIC_UI_PATH;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.util.AntPathMatcher.DEFAULT_PATH_SEPARATOR;

@RequestMapping
public class MetricUIMvcController {

    @Value("classpath:metrics.html")
    private Resource resourceFile;
    private final String actuatorPath;

    public MetricUIMvcController(MetricUIConfigProperties metricUIConfigProperties,
                                 ServerProperties serverProperties,
                                 ManagementServerProperties managementServerProperties,
                                 WebEndpointProperties managementEndpointProperties) {
        String actuatorPath = metricUIConfigProperties.getActuatorPath();
        if (actuatorPath == null) {
            actuatorPath = Optional.ofNullable(managementEndpointProperties).orElseGet(WebEndpointProperties::new).getBasePath();

            // custom address:port is not supported, because we don't have external url address
            if (!samePorts(managementServerProperties, serverProperties)) {
                throw new RuntimeException("Custom management port is not supported. " +
                        "Please set " + ACTUATOR_PATH_PLACEHOLDER + " explicitly");
            }

            String contextPath = Optional.ofNullable(serverProperties)
                    .map(ServerProperties::getServlet)
                    .map(ServerProperties.Servlet::getContextPath)
                    .filter(Predicate.not(DEFAULT_PATH_SEPARATOR::equals))
                    .orElse(null);
            if (contextPath != null) {
                actuatorPath = contextPath + actuatorPath;
            }
        }
        this.actuatorPath = actuatorPath;
    }

    private static boolean samePorts(ManagementServerProperties managementServerProperties,
                                     ServerProperties serverProperties) {
        Integer management = managementServerProperties != null ? managementServerProperties.getPort() : null;
        Integer server = serverProperties != null ? serverProperties.getPort() : null;
        return management == null || management.equals(server) || (server == null && management == 8080);
    }

    @GetMapping(path = METRIC_UI_PATH, produces = TEXT_HTML_VALUE)
    @ResponseBody
    public byte[] metricUi() throws IOException {
        String html = IOUtils.toString(resourceFile.getInputStream(), StandardCharsets.UTF_8);
        return html.replace("$ACTUATOR_URL", actuatorPath).getBytes(StandardCharsets.UTF_8);
    }

}
