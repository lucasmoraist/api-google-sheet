package com.lucasmoraist.register_telecentro.controller;

import com.lucasmoraist.register_telecentro.model.Course;
import com.lucasmoraist.register_telecentro.model.Person;
import com.lucasmoraist.register_telecentro.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/person")
public class PersonController {

    @Autowired
    private PersonService service;

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody Person person) throws IOException {
        this.service.save(person);
        return ResponseEntity.ok().build();
    }

}
