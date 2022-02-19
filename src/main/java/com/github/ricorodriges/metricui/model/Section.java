package com.github.ricorodriges.metricui.model;

import com.github.ricorodriges.metricui.model.view.MetricView;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Section {
    private String name;
    private List<SubSection> subSections;

    @Data
    @AllArgsConstructor
    public static class SubSection {
        private String name;
        private List<? extends MetricView> views;
        private Width width;

        public SubSection(String name, List<? extends MetricView> views) {
            this(name, views, null);
        }
    }
}
