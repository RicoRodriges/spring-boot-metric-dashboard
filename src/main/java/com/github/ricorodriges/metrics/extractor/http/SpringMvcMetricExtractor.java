package com.github.ricorodriges.metrics.extractor.http;

import com.github.ricorodriges.metrics.extractor.PercentileTable;
import com.github.ricorodriges.metrics.model.MeterData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.buildPercentileTable;
import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByName;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpringMvcMetricExtractor {

    public static List<SpringMvcMetricResult> extractResults(Collection<MeterData> meters) {
        return findMetersByName(meters, "http.server.requests")
                .map(m -> {
                    String uri = getURI(m);
                    String method = getMethod(m);
                    String outcome = getOutcome(m);
                    String status = getStatus(m);
                    String exception = getExceptionName(m);
                    return new SpringMvcMetricResult(uri, method, outcome, status, exception, buildPercentileTable(m, meters));
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
    public static class SpringMvcMetricResult {
        private String uri;
        private String method;
        private String outcome;
        private String status;
        private String exception;
        private PercentileTable percentileTable;
    }
}
