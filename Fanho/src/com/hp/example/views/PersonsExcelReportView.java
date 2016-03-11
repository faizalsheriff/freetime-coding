package com.hp.example.views;

import com.hp.example.entities.Person;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractJExcelView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Component("persons;application/vnd.ms-excel")
public final class PersonsExcelReportView extends AbstractJExcelView {
    @Override
    protected void buildExcelDocument(Map<String, Object> model, WritableWorkbook workbook, HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.ms-excel");

        //noinspection unchecked
        List<Person> people = (List<Person>) model.get("persons");

        WritableSheet sheet = workbook.createSheet("Persons", 0);

        sheet.addCell(new Label(0, 0, "First Name"));
        sheet.addCell(new Label(1, 0, "Last Name"));

        int row = 1;
        for (Person p : people) {
            sheet.addCell(new Label(0, row, p.getFirstName()));
            sheet.addCell(new Label(1, row++, p.getLastName()));
        }
    }
}
