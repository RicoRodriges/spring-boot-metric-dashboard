package com.github.ricorodriges.metricui.view.spring.web;

import com.github.ricorodriges.metricui.extractor.spring.web.WebMvcMetricExtractor.WebMvcMetricResult;
import com.github.ricorodriges.metricui.model.Section;
import com.github.ricorodriges.metricui.model.view.TableMetricView;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static com.github.ricorodriges.metricui.view.ViewUtils.buildPercentileTableView;
import static org.springframework.util.CollectionUtils.isEmpty;

@UtilityClass
public final class WebMvcMetricViews {

    public static Section.SubSection buildHttpSubSections(Collection<WebMvcMetricResult> results) {
        if (isEmpty(results)) return null;

        TableMetricView view = buildPercentileTableView(null, List.of("URI", "Method", "Outcome", "Status", "Exception"),
                results,
                Comparator.comparing(WebMvcMetricResult::getUri)
                        .thenComparing(WebMvcMetricResult::getMethod)
                        .thenComparing(WebMvcMetricResult::getOutcome)
                        .thenComparing(WebMvcMetricResult::getStatus)
                        .thenComparing(WebMvcMetricResult::getException),
                r -> List.of(r.getUri(), r.getMethod(), r.getOutcome(), r.getStatus(), r.getException()),
                WebMvcMetricResult::getPercentileTable);
        return new Section.SubSection("HTTP requests", List.of(view));
    }
}
