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
        classes = {MinPropertiesTest.SpringTestApp.class, MetricViewEndpointAutoConfiguration.class, MetricUiMvcAutoConfiguration.class},
        properties = {
                "metricui.enabled=true",
                "management.endpoints.web.exposure.include=health,metricView"
        })
class MinPropertiesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void everythingIsEnabled() throws Exception {
        assertUIAvailable(mockMvc, "", "/metrics", "/actuator");

        assertHealthEndpoint(mockMvc, "", "/actuator", true);
        assertMetricViewEndpoint(mockMvc, "", "/actuator", true);
    }


    @SpringBootApplication
    static class SpringTestApp {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

}