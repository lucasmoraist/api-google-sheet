package com.lucasmoraist.register_telecentro.repository.impl;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.lucasmoraist.register_telecentro.exceptions.ResourceNotFound;
import com.lucasmoraist.register_telecentro.exceptions.SendMailException;
import com.lucasmoraist.register_telecentro.model.Course;
import com.lucasmoraist.register_telecentro.model.Person;
import com.lucasmoraist.register_telecentro.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the PersonRepository interface that uses Google Sheets as the data source.
 * @see PersonRepository
 *
 * @author lucasmoraist
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PersonImpl implements PersonRepository {

    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId;

    private final Sheets sheetsService;

    /**
     * Retrieve next available line
     * @return the next available line
     * @throws IOException if an error occurs while retrieving the next available line
     */
    @Override
    public int getNextAvailableRow() throws IOException {
        String initialRange = "A2:J";
        log.debug("Retrieving the next available row in the spreadsheet");

        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, initialRange)
                .execute();

        List<List<Object>> values = response.getValues();
        int nextRow = (values != null ? values.size() : 0) + 2;

        log.debug("Next available row calculated: {}", nextRow);
        return nextRow;
    }

    /**
     * Saves a person in the Google Sheets.
     * @param person the person to be saved
     * @throws IOException if an error occurs while saving the person
     * @throws SendMailException if an error occurs while sending the email
     */
    @Override
    public void savePersonData(Person person, String range) throws IOException {
        log.debug("Building BatchUpdateValuesRequest for person: {} at range: {}", person.getName(), range);

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

        BatchUpdateValuesRequest request = new BatchUpdateValuesRequest()
                .setValueInputOption("RAW")
                .setData(Collections.singletonList(
                        new ValueRange()
                                .setRange(range)
                                .setValues(data)
                ));

        sheetsService.spreadsheets().values()
                .batchUpdate(spreadsheetId, request)
                .execute();

        log.debug("Successfully saved person data at range: {}", range);
    }

    /**
     * Retrieves a person by their RG.
     * @param rg the RG of the person to be retrieved
     * @return the person with the specified RG
     * @throws IOException if an error occurs while searching for the person
     * @throws ResourceNotFound if the person with the specified RG is not found
     */
    @Override
    public Optional<Person> findPersonByRg(String rg) throws IOException {
        log.info("Searching for person with RG: {}", rg);

        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, "D2:D")
                .execute();

        List<List<Object>> values = response.getValues();
        for (int i = 0; i < values.size(); i++) {
            String currentRg = values.get(i).get(0).toString().trim();
            log.debug("Comparing RG: {} with found RG: {}", rg, currentRg);

            if (currentRg.equalsIgnoreCase(rg.trim())) {
                log.info("Found person with RG: {} at row {}", rg, i + 2);
                return Optional.of(this.getValues("A" + (i + 2) + ":J" + (i + 2)));
            }
        }

        return Optional.empty();
    }

    /**
     * Retrieve values within a range
     * @param range the range to be retrieved
     * @return the person with the specified range
     * @throws IOException if an error occurs while retrieving the values
     */
    private Person getValues(String range) throws IOException {
        log.debug("Retrieving values from range: {}", range);

        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            log.error("No data found at range: {}", range);
            throw new ResourceNotFound("No data found at specified range");
        }

        log.debug("Mapping retrieved data to Person object");
        List<Object> row = values.get(0);
        Course course = new Course(
                !row.isEmpty() ? row.get(0).toString() : null,
                row.size() > 1 ? row.get(1).toString() : null
        );

        return new Person(
                course,
                row.size() > 2 ? row.get(2).toString() : null,
                row.size() > 3 ? row.get(3).toString() : null,
                row.size() > 4 ? LocalDate.parse(row.get(4).toString()) : null,
                row.size() > 5 ? Integer.parseInt(row.get(5).toString()) : 0,
                row.size() > 6 ? row.get(6).toString() : null,
                row.size() > 7 ? row.get(7).toString() : null,
                row.size() > 8 ? row.get(8).toString() : null,
                row.size() > 9 ? row.get(9).toString() : null
        );
    }

    /**
     * Checks if a person with the specified RG and Course Date is already registered.
     * @param rg the RG to be checked
     * @param courseDate the Course Date to be checked
     * @return true if the person is already registered, false otherwise
     * @throws IOException if an error occurs while checking if the person is already registered
     */
    @Override
    public boolean isRgAndCourseDateAlreadyRegistered(String rg, String courseDate) throws IOException {
        log.info("Checking if RG: {} and Course Date: {} are already registered", rg, courseDate);

        String rgRange = "'Incrições Telecentro'!D2:D";
        String courseDateRange = "'Incrições Telecentro'!B2:B";

        ValueRange rgResponse = sheetsService.spreadsheets().values()
                .get(spreadsheetId, rgRange)
                .execute();

        ValueRange courseDateResponse = sheetsService.spreadsheets().values()
                .get(spreadsheetId, courseDateRange)
                .execute();

        List<List<Object>> rgValues = rgResponse.getValues();
        List<List<Object>> courseDateValues = courseDateResponse.getValues();

        if (rgValues == null || courseDateValues == null) {
            log.debug("No data found for RG or Course Date");
            return false;
        }

        for (int i = 0; i < rgValues.size(); i++) {
            String currentRg = rgValues.get(i).get(0).toString().trim();
            String currentCourseDate = courseDateValues.get(i).get(0).toString().trim();

            log.debug("Comparing RG: {} and Course Date: {} with found RG: {} and Course Date: {}", rg, courseDate, currentRg, currentCourseDate);

            if (currentRg.equalsIgnoreCase(rg.trim()) && currentCourseDate.equalsIgnoreCase(courseDate.trim())) {
                log.info("Match found: RG: {} and Course Date: {}", rg, courseDate);
                return true;
            }
        }

        log.info("No match found for RG: {} and Course Date: {}", rg, courseDate);
        return false;
    }
}
