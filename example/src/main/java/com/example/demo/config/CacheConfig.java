package com.example.demo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${metrics.enabled}")
    private boolean stats;

    @Bean
    public Cache personCache() {
        return new CaffeineCache("person-cache", newBuilder(stats)
                .expireAfterWrite(3, TimeUnit.HOURS)
                .build());
    }

    @Bean
    public Cache planetCache() {
        return new CaffeineCache("planet-cache", newBuilder(stats)
                .expireAfterWrite(3, TimeUnit.HOURS)
                .build());
    }

    public static Caffeine<Object, Object> newBuilder(boolean stats) {
        Caffeine<Object, Object> b = Caffeine.newBuilder();
        return stats ? b.recordStats() : b;
    }
}
