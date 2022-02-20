package com.example.demo.service;

import com.example.demo.feign.PlanetAPI;
import com.example.demo.feign.PlanetAPI.Planet;
import com.example.demo.repo.db2.PlanetRepository;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanetService {

    private final PlanetRepository planetRepository;
    private final PlanetAPI planetAPI;

    @Counted
    @Cacheable("planet-cache")
    public Optional<Planet> getById(int id) {
        return planetRepository.findById(id)
                .map(v -> new Planet(v.getId(), v.getName(), v.getCreated()))
                .or(() -> planetAPI.getById(id));
    }
}
