package com.lucasmoraist.register_telecentro.repository;

import com.lucasmoraist.register_telecentro.model.Course;
import com.lucasmoraist.register_telecentro.model.Person;

import java.io.IOException;

public interface PersonRepository {
    void save(Person person) throws IOException;
    Person findByRg(String rg);
    Person findByName(String name);
}
