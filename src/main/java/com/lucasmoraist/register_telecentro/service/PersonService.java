package com.lucasmoraist.register_telecentro.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.lucasmoraist.register_telecentro.model.Person;
import com.lucasmoraist.register_telecentro.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PersonService  {

    @Autowired
    private Sheets sheetsService;

//    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId = "18SBKxFrS64Ufy_v-N_NSixH9BXchafJIQB4HFe0Tntk";

    private final String initialRange = "A2:J";

    public void save(Person person) throws IOException {
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

        this.sheetsService.spreadsheets().values()
                .batchUpdate(spreadsheetId, body)
                .execute();
    }
}
