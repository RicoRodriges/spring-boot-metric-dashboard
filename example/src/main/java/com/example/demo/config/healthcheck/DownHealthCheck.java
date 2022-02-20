package com.example.demo.config.healthcheck;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
public class DownHealthCheck extends AbstractHealthIndicator {

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        builder.down(new RuntimeException("Exception description"))
                .withDetail("abc", "It's always down");
    }
}
