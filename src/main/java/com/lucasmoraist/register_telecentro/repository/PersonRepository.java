package com.lucasmoraist.register_telecentro.repository;

import com.lucasmoraist.register_telecentro.model.Person;

import java.io.IOException;

/**
 * This interface defines the methods that must be implemented by a class that will be responsible for saving
 * and retrieving
 *
 * @author lucasmoraist
 */
public interface PersonRepository {
    void save(Person person) throws IOException;
    Person getPersonByRg(String rg) throws IOException;
}
