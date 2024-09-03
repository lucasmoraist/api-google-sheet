package com.lucasmoraist.register_telecentro.service;

import com.lucasmoraist.register_telecentro.exceptions.ResourceNotFound;
import com.lucasmoraist.register_telecentro.exceptions.RgRegistered;
import com.lucasmoraist.register_telecentro.exceptions.SendMailException;
import com.lucasmoraist.register_telecentro.model.Person;
import com.lucasmoraist.register_telecentro.repository.impl.PersonImpl;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service class responsible for handling operations related to the Person entity.
 * It implements the PersonRepository interface, which defines the methods that must be implemented.
 *
 * @author lucasmoraist
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PersonService {

    private final PersonImpl sheetsImpl;
    private final EmailService emailService;

    public void save(Person person) throws IOException {
        log.info("Starting save operation for person: {}", person.getName());

        this.verifyPerson(person);

        int nextRow = sheetsImpl.getNextAvailableRow();
        log.debug("Next available row for insertion: {}", nextRow);

        String nextRange = String.format("'Incrições Telecentro'!A%d:J%d", nextRow, nextRow);
        log.debug("Computed range for insertion: {}", nextRange);

        sheetsImpl.savePersonData(person, nextRange);
        log.info("Successfully saved person data at range: {}", nextRange);

        try {
            emailService.sendEmail(person);
            log.info("Email sent successfully to: {}", person.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send email for person: {}", person.getName(), e);
            throw new SendMailException();
        }
    }

    public List<Person> listPersonByRg(String rg) throws IOException {
        log.info("Searching for person with RG: {}", rg);

        return sheetsImpl.listPersonByRg(rg);
    }

    private void verifyPerson(Person person) throws IOException {
        if (person.getRg() == null || person.getCourse() == null || person.getCourse().getDateAndTime() == null) {
            log.error("Required fields are missing in the person object. Aborting save operation. RG: {}, Course: {}", person.getRg(), person.getCourse());
            throw new IllegalArgumentException("Required fields are missing in the person object");
        }
        if (sheetsImpl.isRgAndCourseDateAlreadyRegistered(person.getRg(), person.getCourse().getDateAndTime())) {
            log.error("RG: {} and Course Date: {} are already registered. Aborting save operation.", person.getRg(), person.getCourse().getDateAndTime());
            throw new RgRegistered();
        }
    }
}
