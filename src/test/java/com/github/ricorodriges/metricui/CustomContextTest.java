package com.github.ricorodriges.metricui;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.ricorodriges.metricui.TestUtils.*;

@AutoConfigureMockMvc
@SpringBootTest(
        classes = {CustomContextTest.SpringTestApp.class, MetricViewEndpointAutoConfiguration.class, MetricUiMvcAutoConfiguration.class},
        properties = {
                "metricui.enabled=true", "metricui.path=/metr", "metricui.actuator-path=https://s.com:9001/manag/actuat",
                "server.port=8080", "server.servlet.context-path=/demo/app",
                "management.server.port=9001", "management.server.base-path=/manag", "management.endpoints.web.base-path=/actuat",
                "management.endpoints.web.exposure.include=health,metricView"
        })
class CustomContextTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void customManagementPort() throws Exception {
        assertUIAvailable(mockMvc, "/demo/app", "/metr", "https://s.com:9001/manag/actuat");
        // another context due to custom management port
        assertHealthEndpoint(mockMvc, "/manag", "/actuat", false);
        assertMetricViewEndpoint(mockMvc, "/manag", "/actuat", false);
    }


    @SpringBootApplication
    static class SpringTestApp {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

}