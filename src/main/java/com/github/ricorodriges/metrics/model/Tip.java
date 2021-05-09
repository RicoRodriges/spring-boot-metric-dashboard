package com.github.ricorodriges.metrics.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Tip {
    private String title;
    private String text;
    private boolean html;
}
