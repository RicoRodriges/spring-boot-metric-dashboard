package com.github.ricorodriges.metrics.view.http;

import com.github.ricorodriges.metrics.extractor.http.SpringMvcMetricExtractor.SpringMvcMetricResult;
import com.github.ricorodriges.metrics.model.Section;
import com.github.ricorodriges.metrics.model.view.TableMetricView;
import com.github.ricorodriges.metrics.view.MetricViewUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpringMvcMetricViews {

    public static Section.SubSection buildHttpSubSections(Collection<SpringMvcMetricResult> results) {
        if (results.isEmpty()) return null;

        TableMetricView view = MetricViewUtils.buildPercentileTableView(null, results,
                Arrays.asList("URI", "Method", "Outcome", "Status", "Exception"),
                r -> Arrays.asList(r.getUri(), r.getMethod(), r.getOutcome(), r.getStatus(), r.getException()),
                Comparator.comparing(SpringMvcMetricResult::getUri)
                        .thenComparing(SpringMvcMetricResult::getMethod)
                        .thenComparing(SpringMvcMetricResult::getOutcome)
                        .thenComparing(SpringMvcMetricResult::getStatus)
                        .thenComparing(SpringMvcMetricResult::getException),
                SpringMvcMetricResult::getPercentileTable);
        return new Section.SubSection("HTTP requests", Collections.singletonList(view));
    }
}
