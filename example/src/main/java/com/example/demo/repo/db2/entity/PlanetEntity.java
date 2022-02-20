package com.example.demo.repo.db2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@Entity
public class PlanetEntity {
    @Id
    private Integer id;
    private String name;
    private ZonedDateTime created;
}
