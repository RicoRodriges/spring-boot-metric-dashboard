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
        classes = {NoPropertiesTest.SpringTestApp.class, MetricViewEndpointAutoConfiguration.class, MetricUiMvcAutoConfiguration.class}
)
class NoPropertiesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void everythingIsDisabled() throws Exception {
        assertUIIsNotAvailable(mockMvc, "", "/metrics");

        assertHealthEndpoint(mockMvc, "", "/actuator", true);
        assertMetricViewEndpoint(mockMvc, "", "/actuator", false);
    }


    @SpringBootApplication
    static class SpringTestApp {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

}