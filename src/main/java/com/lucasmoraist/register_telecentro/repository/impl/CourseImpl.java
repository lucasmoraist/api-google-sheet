package com.lucasmoraist.register_telecentro.repository.impl;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.lucasmoraist.register_telecentro.model.Course;
import com.lucasmoraist.register_telecentro.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
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
        String range = "Cursos!A2:Z";
        log.info("Retrieving course information from range: {}", range);

        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        List<Course> courseInfoList = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            log.info("No course information found in the specified range.");
            return courseInfoList;
        }

        for (List<Object> row : values) {
            if (!row.isEmpty()) {
                String courseName = row.get(0).toString();
                StringBuilder dateTimesBuilder = new StringBuilder();

                for (int i = 1; i < row.size(); i++) {
                    if (i > 1) {
                        dateTimesBuilder.append(", ");
                    }
                    dateTimesBuilder.append(row.get(i).toString());
                }

                String dateTimes = dateTimesBuilder.toString();
                Course courseInfo = new Course(courseName, dateTimes);
                courseInfoList.add(courseInfo);
            }
        }

        log.info("Successfully retrieved {} course information entries.", courseInfoList.size());
        return courseInfoList;
    }


}

