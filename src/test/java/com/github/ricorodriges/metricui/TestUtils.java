package com.github.ricorodriges.metricui;

import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UtilityClass
public final class TestUtils {

    public static void assertUIAvailable(MockMvc mockMvc, String contextPath, String url, String actuatorUrl) throws Exception {
        mockMvc.perform(get(contextPath + url).contextPath(contextPath))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(content().string(not(containsString("var ACTUATOR_URL = \"$ACTUATOR_URL\";"))))
                .andExpect(content().string(containsString("var ACTUATOR_URL = \"" + actuatorUrl + "\";")))
        ;
    }

    public static void assertUIIsNotAvailable(MockMvc mockMvc, String contextPath, String url) throws Exception {
        mockMvc.perform(get(contextPath + url).contextPath(contextPath))
                .andExpect(status().isNotFound())
        ;
    }

    public static void assertHealthEndpoint(MockMvc mockMvc, String contextPath, String actuatorUrl, boolean available) throws Exception {
        mockMvc.perform(get(contextPath + actuatorUrl + "/health").contextPath(contextPath))
                .andExpect(available ? status().isOk() : status().isNotFound())
        ;
    }

    public static void assertMetricViewEndpoint(MockMvc mockMvc, String contextPath, String actuatorUrl, boolean available) throws Exception {
        mockMvc.perform(get(contextPath + actuatorUrl + "/metricView").contextPath(contextPath))
                .andExpect(available ? status().isOk() : status().isNotFound())
        ;
    }
}
