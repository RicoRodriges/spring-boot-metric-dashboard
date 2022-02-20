package com.example.demo.controller;

import com.example.demo.feign.PersonAPI.Person;
import com.example.demo.feign.PlanetAPI.Planet;
import com.example.demo.service.PersonService;
import com.example.demo.service.PlanetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("${api.root}")
@RequiredArgsConstructor
public class SomeController {

    private final PersonService personService;
    private final PlanetService planetService;

    @GetMapping(value = "/people/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Person> personById(@PathVariable("id") int id) {
        return ResponseEntity.of(personService.getById(id));
    }

    @GetMapping(value = "/planet/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Planet> planetById(@PathVariable("id") int id) {
        return ResponseEntity.of(planetService.getById(id));
    }
}
