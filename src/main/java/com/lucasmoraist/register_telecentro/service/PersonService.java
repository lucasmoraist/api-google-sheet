package com.lucasmoraist.register_telecentro.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.lucasmoraist.register_telecentro.exceptions.ResourceNotFound;
import com.lucasmoraist.register_telecentro.exceptions.SendMailException;
import com.lucasmoraist.register_telecentro.model.Course;
import com.lucasmoraist.register_telecentro.model.Person;
import com.lucasmoraist.register_telecentro.repository.PersonRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service class responsible for handling operations related to the Person entity.
 * It implements the PersonRepository interface, which defines the methods that must be implemented.
 *
 * @author lucasmoraist
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PersonService implements PersonRepository {

    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId;

    private final Sheets sheetsService;
    private final EmailService emailService;

    /**
     * Saves a person in the Google Sheets.
     * @param person the person to be saved
     * @throws IOException if an error occurs while saving the person
     * @throws SendMailException if an error occurs while sending the email
     */
    public void save(Person person) throws IOException {
        log.info("Starting save operation for person: {}", person.getName());

        try {
            int nextRow = this.getNextAvailableRow();
            log.debug("Next available row for insertion: {}", nextRow);

            String nextRange = String.format("A%d:J%d", nextRow, nextRow);
            log.debug("Computed range for insertion: {}", nextRange);

            BatchUpdateValuesRequest request = buildBatchUpdateRequest(person, nextRange);
            sheetsService.spreadsheets().values()
                    .batchUpdate(spreadsheetId, request)
                    .execute();

            log.info("Successfully saved person data at range: {}", nextRange);
            this.emailService.sendEmail(person);
            log.info("Email sent successfully to: {}", person.getEmail());
        } catch (IOException e){
            log.error("Failed to save person data", e);
            throw new IOException();
        } catch (MessagingException e) {
            log.error("Failed to send email for person: {}", person.getName(), e);
            throw new SendMailException();
        }
    }

    /**
     * Retrieve next available line
     * @return the next available line
     * @throws IOException if an error occurs while retrieving the next available line
     */
    private int getNextAvailableRow() throws IOException {
        String initialRange = "'Incrições Telecentro'!A2:J";
        log.debug("Retrieving the next available row in the spreadsheet");

        ValueRange response = this.sheetsService.spreadsheets().values()
                .get(spreadsheetId, initialRange)
                .execute();

        List<List<Object>> values = response.getValues();
        int nextRow = (values != null ? values.size() : 0) + 2;

        log.debug("Next available row calculated: {}", nextRow);
        return nextRow;
    }

    /**
     * Builds a BatchUpdateValuesRequest object with the person data.
     * @param person the person to be saved
     * @param range the range where the person data will be saved
     * @return the BatchUpdateValuesRequest object
     */
    private BatchUpdateValuesRequest buildBatchUpdateRequest(Person person, String range) {
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

        log.debug("BatchUpdateValuesRequest built successfully for person: {}", person.getName());
        return new BatchUpdateValuesRequest()
                .setValueInputOption("RAW")
                .setData(Collections.singletonList(
                        new ValueRange()
                                .setRange(range)
                                .setValues(data)
                ));
    }

    /**
     * Retrieves a person by their RG.
     * @param rg the RG of the person to be retrieved
     * @return the person with the specified RG
     * @throws IOException if an error occurs while searching for the person
     * @throws ResourceNotFound if the person with the specified RG is not found
     */
    public Person getPersonByRg(String rg) throws IOException {
        log.info("Searching for person with RG: {}", rg);

        try {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, "D2:D")
                    .execute();

            List<List<Object>> values = response.getValues();
            for (int i = 0; i < values.size(); i++) {
                String currentRg = values.get(i).get(0).toString().trim();
                log.debug("Comparing RG: {} with found RG: {}", rg, currentRg);

                if (currentRg.equalsIgnoreCase(rg.trim())) {
                    log.info("Found person with RG: {} at row {}", rg, i + 2);
                    return getValues("A" + (i + 2) + ":J" + (i + 2));
                }
            }
        } catch (IOException e) {
            log.error("Error occurred while searching for RG: {}", rg, e);
            throw e;
        }

        log.error("Person with RG: {} not found", rg);
        throw new ResourceNotFound("Person with RG not found");
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

        Person person = new Person(
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

        log.debug("Successfully mapped data to Person object: {}", person.getName());
        return person;
    }
}
