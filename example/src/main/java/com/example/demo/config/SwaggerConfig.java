package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI publicOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Some API").description("Some API desc").version("v1"));
    }

    @Bean
    public GroupedOpenApi publicAPI() {
//        SpringDocUtils.getConfig().addRestControllers(SomeController.class);
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToScan("com.example.demo.controller")
                .build();
    }
}
