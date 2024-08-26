package com.lucasmoraist.register_telecentro.service;

import com.lucasmoraist.register_telecentro.model.Course;
import com.lucasmoraist.register_telecentro.model.dto.UpdateCourse;
import com.lucasmoraist.register_telecentro.repository.impl.CourseImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class CourseSevice {

    @Autowired
    private CourseImpl repository;

    public List<Course> findAll() throws IOException {
        return this.repository.listAll();
    }

    public void updateCourseTimes(UpdateCourse request) throws IOException {
        this.repository.updateCourseDateTime(request);
    }

}
