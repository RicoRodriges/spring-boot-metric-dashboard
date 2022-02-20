package com.example.demo.repo.db2;

import com.example.demo.repo.db2.entity.PlanetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanetRepository extends JpaRepository<PlanetEntity, Integer> {
}
