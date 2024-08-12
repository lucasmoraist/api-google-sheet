package com.lucasmoraist.register_telecentro.controller;

import com.lucasmoraist.register_telecentro.model.Person;
import com.lucasmoraist.register_telecentro.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<Person> getByName(@RequestParam String rg) throws IOException {
        return ResponseEntity.ok().body(this.service.getPersonByRg(rg));
    }

}
