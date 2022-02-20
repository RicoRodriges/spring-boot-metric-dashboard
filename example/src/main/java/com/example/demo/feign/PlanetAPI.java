package com.example.demo.feign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "planet", url = "https://swapi.dev/api")
public interface PlanetAPI {

    @GetMapping(value = "/planets/{id}", produces = APPLICATION_JSON_VALUE)
    Optional<Planet> getById(@PathVariable int id);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class Planet {
        private int id;
        private String name;
        private ZonedDateTime created;
    }
}
