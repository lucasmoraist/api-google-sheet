package com.lucasmoraist.register_telecentro.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * This class represents a course that will be registered in the system
 *
 * @author lucasmoraist
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    private String nameCourse;
    private List<String> dateAndTime;

}
