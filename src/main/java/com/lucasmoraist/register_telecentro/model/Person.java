package com.lucasmoraist.register_telecentro.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * This class represents a person that will be registered in the system
 *
 * @author lucasmoraist
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    private Course course;
    private String name;
    private String rg;
    private LocalDate birthDate;
    private int age;
    private String address;
    private String email;
    private String phoneNumber;
    private String isConfirmed;

}
