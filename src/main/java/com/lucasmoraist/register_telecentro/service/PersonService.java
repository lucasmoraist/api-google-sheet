package com.lucasmoraist.register_telecentro.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.lucasmoraist.register_telecentro.exceptions.ResourceNotFound;
import com.lucasmoraist.register_telecentro.model.Course;
import com.lucasmoraist.register_telecentro.model.Person;
import com.lucasmoraist.register_telecentro.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class PersonService implements PersonRepository {

    @Autowired
    private Sheets sheetsService;

    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId;

    private final String initialRange = "A2:J";

    public void save(Person person) throws IOException {
        log.info("Starting save operation for person: {}", person.getName());

        ValueRange response = this.sheetsService.spreadsheets().values()
                .get(spreadsheetId, initialRange)
                .execute();

        List<List<Object>> values = response.getValues();
        int nextRow = (values != null ? values.size() : 0) + 2;
        String nextRange = String.format("A%d:J%d", nextRow, nextRow);

        List<List<Object>> data = new ArrayList<>();
        data.add(List.of(
                person.getCourse().getNameCourse(),
                person.getCourse().getDateAndTime(),
                person.getName(),
                person.getRg(),
                person.getBirthDate().toString(),
                String.valueOf(person.getAge()),
                person.getAddress(),
                person.getEmail(),
                person.getPhoneNumber(),
                person.getIsConfirmed()
        ));

        BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
                .setValueInputOption("RAW")
                .setData(Collections.singletonList(
                        new ValueRange()
                                .setRange(nextRange)
                                .setValues(data)
                ));

        try {
            this.sheetsService.spreadsheets().values()
                    .batchUpdate(spreadsheetId, body)
                    .execute();
            log.info("Successfully saved person data at range: {}", nextRange);
        } catch (IOException e) {
            log.error("Failed to save person data at range: {}", nextRange, e);
            throw e;
        }
    }

    public Person getPersonByRg(String rg) throws IOException {
        log.info("Searching for person with RG: {}", rg);

        ValueRange response = this.sheetsService.spreadsheets().values()
                .get(spreadsheetId, "D2:D")
                .execute();

        List<List<Object>> values = response.getValues();
        for (int i = 0; i < values.size(); i++) {
            String currentRg = values.get(i).get(0).toString().trim();
            if (currentRg.equalsIgnoreCase(rg.trim())) {
                log.info("Found person with RG: {} at row {}", rg, i + 2);
                return this.getValues("A" + (i + 2) + ":J" + (i + 2));
            }
        }
        log.error("Person with RG: {} not found", rg);
        throw new ResourceNotFound("Person with RG not found");
    }

    private Person getValues(String range) throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        List<Object> row = values.get(0);

        String nameCourse = !row.isEmpty() ? row.get(0).toString() : null;
        String dateAndTime = row.size() > 1 ? row.get(1).toString() : null;
        Course course = new Course(nameCourse, dateAndTime);

        String name = row.size() > 2 ? row.get(2).toString() : null;
        String rg = row.size() > 3 ? row.get(3).toString() : null;

        String birthDateString = row.size() > 4 ? row.get(4).toString() : null;
        LocalDate birthDate = birthDateString != null ? LocalDate.parse(birthDateString) : null;

        String ageString = row.size() > 5 ? row.get(5).toString() : null;
        int age = ageString != null ? Integer.parseInt(ageString) : 0;

        String address = row.size() > 6 ? row.get(6).toString() : null;
        String email = row.size() > 7 ? row.get(7).toString() : null;
        String phoneNumber = row.size() > 8 ? row.get(8).toString() : null;
        String isConfirmed = row.size() > 9 ? row.get(9).toString() : null;

        return new Person(course, name, rg, birthDate, age, address, email, phoneNumber, isConfirmed);

    }
}
