package com.lucasmoraist.register_telecentro.controller;

import com.lucasmoraist.register_telecentro.model.Person;
import com.lucasmoraist.register_telecentro.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

/**
 * This class is responsible for receiving requests from the client and returning responses
 *
 * @see PersonService
 * @see Person
 *
 * @author lucasmoraist
 */
@Slf4j
@RestController
@RequestMapping("/person")
public class PersonController {

    @Autowired
    private PersonService service;

    /**
     * This method is responsible for receiving a request from the client to save a person in the system
     * @param person the person to be saved
     * @return a response to the client
     * @throws IOException if an error occurs while saving the person
     */
    @PostMapping
    public ResponseEntity<Void> save(@RequestBody Person person) throws IOException {
        log.info("Received request to save person: {}", person.getName());
        this.service.save(person);
        log.info("Person saved successfully: {}", person.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * This method is responsible for receiving a request from the client to retrieve a person from the system.
     *
     * @param rg the rg of the person to be retrieved
     * @return a response to the client
     * @throws IOException if an error occurs while retrieving the person
     */
    @GetMapping
    public ResponseEntity<Optional<Person>> getByRg(@RequestParam String rg) throws IOException {
        log.info("Received request to retrieve person with RG: {}", rg);
        Optional<Person> person = this.service.getPersonByRg(rg);
        log.info("Person retrieved successfully with RG: {}", rg);
        return ResponseEntity.ok().body(person);
    }

}
