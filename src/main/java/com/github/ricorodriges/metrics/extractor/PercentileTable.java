package com.github.ricorodriges.metrics.extractor;

import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Data
public class PercentileTable {
    private long count;
    private Duration total;
    private Duration max;
    private Map<Double, Duration> percentiles = new HashMap<>();

    public PercentileTable(long count, Duration total, Duration max) {
        this.count = count;
        this.total = total;
        this.max = max;
    }
}
