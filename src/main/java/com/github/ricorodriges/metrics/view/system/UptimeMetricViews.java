package com.github.ricorodriges.metrics.view.system;

import com.github.ricorodriges.metrics.extractor.system.UptimeMetricExtractor.UptimeMetricResult;
import com.github.ricorodriges.metrics.model.view.LabelMetricView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

import static com.github.ricorodriges.metrics.view.MetricViewUtils.duration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UptimeMetricViews {

    public static LabelMetricView buildUptimeLabel(UptimeMetricResult r) {
        String startDate = DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm:ss Z").format(r.getStartDate());
        String uptime = duration(r.getUptime());
        return new LabelMetricView("JVM Uptime: " + uptime + ", since " + startDate);
    }
}
