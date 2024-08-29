package com.lucasmoraist.register_telecentro.repository;

import com.lucasmoraist.register_telecentro.model.Course;

import java.io.IOException;
import java.util.List;

public interface CourseRepository {
    List<Course> listAll() throws IOException;
}
