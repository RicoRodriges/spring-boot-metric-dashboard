package com.github.ricorodriges.metricui.view.micrometer.system;

import com.github.ricorodriges.metricui.extractor.micrometer.system.UptimeMetricExtractor.UptimeMetricResult;
import com.github.ricorodriges.metricui.model.view.LabelMetricView;
import lombok.experimental.UtilityClass;

import java.time.format.DateTimeFormatter;

import static com.github.ricorodriges.metricui.view.ViewUtils.durationToString;

@UtilityClass
public final class UptimeMetricViews {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm:ss Z");

    public static LabelMetricView buildUptimeLabel(UptimeMetricResult r) {
        String startDate = FORMAT.format(r.getStartDate());
        String uptime = durationToString(r.getUptime());
        return new LabelMetricView("JVM Uptime: " + uptime + ", since " + startDate);
    }
}
