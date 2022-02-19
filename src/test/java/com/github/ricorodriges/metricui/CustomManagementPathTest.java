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
        classes = {CustomManagementPathTest.SpringTestApp.class, MetricViewEndpointAutoConfiguration.class, MetricUiMvcAutoConfiguration.class},
        properties = {
                "metricui.enabled=true", "metricui.path=/metr",
                "server.port=8081", "server.servlet.context-path=/demo/app",
                "management.server.port=8081", "management.server.base-path=/manag", "management.endpoints.web.base-path=/actuat",
                "management.endpoints.web.exposure.include=health,metricView"
        })
class CustomManagementPathTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void customPathIsOk() throws Exception {
        assertUIAvailable(mockMvc, "/demo/app", "/metr", "/demo/app/actuat");

        assertHealthEndpoint(mockMvc, "/demo/app", "/actuat", true);
        assertMetricViewEndpoint(mockMvc, "/demo/app", "/actuat", true);
    }


    @SpringBootApplication
    static class SpringTestApp {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

}