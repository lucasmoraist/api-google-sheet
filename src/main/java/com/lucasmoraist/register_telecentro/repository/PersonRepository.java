package com.lucasmoraist.register_telecentro.repository;

import com.lucasmoraist.register_telecentro.model.Person;

import java.io.IOException;
import java.util.Optional;

/**
 * This interface defines the methods that must be implemented by a class that will be responsible for saving
 * and retrieving
 *
 * @author lucasmoraist
 */
public interface PersonRepository {
    int getNextAvailableRow() throws IOException;
    void savePersonData(Person person, String range) throws IOException;
    Optional<Person> findPersonByRg(String rg) throws IOException;
    boolean isRgAndCourseDateAlreadyRegistered(String rg, String courseDate) throws IOException;
}
