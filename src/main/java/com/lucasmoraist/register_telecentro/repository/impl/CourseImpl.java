package com.lucasmoraist.register_telecentro.repository.impl;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.lucasmoraist.register_telecentro.exceptions.ResourceNotFound;
import com.lucasmoraist.register_telecentro.model.Course;
import com.lucasmoraist.register_telecentro.model.dto.UpdateCourse;
import com.lucasmoraist.register_telecentro.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseImpl implements CourseRepository {

    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId;

    private final Sheets sheetsService;

    /**
     * Retrieve a list of course information containing name and date/time.
     *
     * @return a list of CourseInfo objects containing course name and date/time
     * @throws IOException if an error occurs while retrieving data from the Google Sheets
     */
    @Override
    public List<Course> listAll() throws IOException {
        String range = "Cursos!A2:Z"; // Ajustado para considerar múltiplas colunas de horários
        log.info("Retrieving course information from range: {}", range);

        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        List<Course> courseInfoList = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            log.info("No course information found in the specified range.");
            return courseInfoList; // Retorna lista vazia se não houver dados
        }

        for (List<Object> row : values) {
            if (!row.isEmpty()) {
                String courseName = row.get(0).toString(); // Nome do curso na coluna A
                List<String> dateTimes = new ArrayList<>();

                // Adiciona todas as datas e horários a partir da coluna B
                for (int i = 1; i < row.size(); i++) {
                    dateTimes.add(row.get(i).toString());
                }

                Course courseInfo = new Course(courseName, dateTimes);
                courseInfoList.add(courseInfo);
            }
        }

        log.info("Successfully retrieved {} course information entries.", courseInfoList.size());
        return courseInfoList;
    }

    /**
     * Updates the morning and afternoon times for a given course.
     * 
     * @param dto the new afternoon time
     * @throws IOException if an error occurs while updating the course information
     */
    public void updateCourseDateTime(UpdateCourse dto) throws IOException {
        // Find the row of the course
        int rowIndex = findCourseRowIndex(dto.courseName());
        if (rowIndex == -1) {
            log.error("Course with name '{}' not found.", dto.courseName());
            throw new ResourceNotFound("Course not found");
        }

        // Update the times in columns B and C
        String range = "Cursos!B" + (rowIndex + 2) + ":C" + (rowIndex + 2); // Assuming data starts from row 2
        List<List<Object>> values = Collections.singletonList(
                List.of(dto.morningTime(), dto.afternoonTime())
        );

        ValueRange body = new ValueRange()
                .setValues(values);

        sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        log.info("Successfully updated times for course '{}' at row {}", dto.courseName(), rowIndex + 2);
    }

    /**
     * Finds the row index for a given course name.
     *
     * @param courseName the name of the course
     * @return the index of the row, or -1 if the course is not found
     * @throws IOException if an error occurs while searching for the course
     */
    private int findCourseRowIndex(String courseName) throws IOException {
        String range = "Cursos!A2:A"; // Assume course names are in column A
        log.info("Searching for course with name '{}' in column A", courseName);

        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                String currentCourseName = values.get(i).get(0).toString().trim();
                if (currentCourseName.equalsIgnoreCase(courseName.trim())) {
                    return i; // Row index relative to the range start
                }
            }
        }

        return -1; // Course not found
    }
}

