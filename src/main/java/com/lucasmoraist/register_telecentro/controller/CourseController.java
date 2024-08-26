package com.lucasmoraist.register_telecentro.controller;

import com.lucasmoraist.register_telecentro.exceptions.ResourceNotFound;
import com.lucasmoraist.register_telecentro.model.Course;
import com.lucasmoraist.register_telecentro.model.dto.UpdateCourse;
import com.lucasmoraist.register_telecentro.service.CourseSevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping("/courses")
@Slf4j
public class CourseController {

    @Autowired
    private CourseSevice service;

    @GetMapping
    public List<Course> listAll() throws IOException {
        return this.service.findAll();
    }

    /**
     * Updates the morning and afternoon times for a given course.
     *
     * @param request the request body containing course name and new times
     * @return ResponseEntity with status code indicating success or failure
     */
    @PutMapping("/update-time")
    public ResponseEntity<String> updateCourseTime(@RequestBody UpdateCourse request) {
        try {
            service.updateCourseTimes(request);
            return ResponseEntity.ok("Course times updated successfully");
        } catch (IOException e) {
            log.error("Error updating course times", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update course times");
        } catch (ResourceNotFound e) {
            log.error("Course not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Course not found");
        }
    }

}
