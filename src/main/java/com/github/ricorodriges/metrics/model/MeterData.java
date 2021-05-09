package com.github.ricorodriges.metrics.model;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Statistic;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;

@Getter
@AllArgsConstructor
public class MeterData {
    private final String name;
    private final String description;
    private final Map<String, String> tags;
    private final Meter.Type type;
    private final String unit;
    private final Collection<Measurement> measurements;

    @Getter
    @AllArgsConstructor
    public static class Measurement {
        private final Statistic type;
        private final double value;
    }
}
