package com.example.demo.repo.db1.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@Entity
public class PersonEntity {
    @Id
    private Integer id;
    private String name;
    private ZonedDateTime created;
}
