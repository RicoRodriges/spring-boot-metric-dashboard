package com.example.demo.service;

import com.example.demo.feign.PersonAPI;
import com.example.demo.feign.PersonAPI.Person;
import com.example.demo.repo.db1.PersonRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonAPI personAPI;

    @Timed
    @Cacheable("person-cache")
    public Optional<Person> getById(int id) {
        return personRepository.findById(id)
                .map(v -> new Person(v.getId(), v.getName(), v.getCreated()))
                .or(() -> personAPI.getById(id));
    }
}
