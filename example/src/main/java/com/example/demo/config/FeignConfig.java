package com.example.demo.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableFeignClients(basePackages = "com.example.demo.feign")
public class FeignConfig {
}
