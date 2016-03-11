package com.hp.example.views;

import com.hp.example.entities.Person;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

@Component("persons;text/csv")
public final class PersonsCsvView implements View {
    @Override
    public String getContentType() {
        return "text/csv";
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        //noinspection unchecked
        List<Person> persons = (List<Person>) model.get("persons");

        try (CsvBeanWriter writer = new CsvBeanWriter(new OutputStreamWriter(response.getOutputStream()), CsvPreference.STANDARD_PREFERENCE)) {
            writer.writeHeader("firstName", "lastName");
            for (Person p : persons) {
                writer.write(p, "firstName", "lastName");
            }
        }
    }
}
