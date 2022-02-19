package com.github.ricorodriges.metricui.extractor.spring.web;

import com.github.ricorodriges.metricui.extractor.PercentileTable;
import com.github.ricorodriges.metricui.model.MeterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.buildPercentileTable;
import static com.github.ricorodriges.metricui.extractor.ExtractorUtils.findMetersByName;

/**
 * @see org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration
 */
@UtilityClass
public final class WebMvcMetricExtractor {

    public static List<WebMvcMetricResult> extractResults(Collection<MeterData> meters, String metricName) {
        return findMetersByName(meters, metricName != null ? metricName : "http.server.requests")
                .map(m -> {
                    String uri = getURI(m);
                    String method = getMethod(m);
                    String outcome = getOutcome(m);
                    String status = getStatus(m);
                    String exception = getExceptionName(m);
                    return new WebMvcMetricResult(uri, method, outcome, status, exception, buildPercentileTable(m, meters));
                })
                .collect(Collectors.toList());
    }

    private static String getURI(MeterData m) {
        return m.getTags().get("uri");
    }

    private static String getMethod(MeterData m) {
        return m.getTags().get("method");
    }

    private static String getExceptionName(MeterData m) {
        String exception = m.getTags().get("exception");
        if ("None".equals(exception)) {
            exception = "";
        }
        return exception;
    }

    private static String getStatus(MeterData m) {
        return m.getTags().get("status");
    }

    private static String getOutcome(MeterData m) {
        return m.getTags().get("outcome");
    }

    @Data
    @AllArgsConstructor
    public static class WebMvcMetricResult {
        private String uri;
        private String method;
        private String outcome;
        private String status;
        private String exception;
        private PercentileTable percentileTable;
    }
}
