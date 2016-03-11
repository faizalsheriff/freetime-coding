package com.hp.example.controllers;

import com.hp.example.entities.Employee;
import com.hp.example.entities.Person;
import com.hp.example.services.PersonService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;

import java.io.IOException;
import java.util.List;

@Controller
public final class PersonController {
    @Autowired
    private PersonService personService;

    @RequestMapping(value = "/persons", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<Person> getPersons() {
        return personService.readAllPersons();
    }

    @RequestMapping(value = "/persons/{id:[\\p{Digit}]+}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Person> getPerson(@PathVariable long id) {
        Person p = personService.readPerson(id);

        if (p == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(p, HttpStatus.OK);
    }

    @RequestMapping(value = "/persons", produces = {"text/html", "application/vnd.ms-excel", "text/csv"})
    @ResponseStatus(HttpStatus.OK)
    public Model personsWithView(Model model) {
        return model.addAttribute("persons", getPersons());
    }

    @RequestMapping(value = "/persons", produces = "application/json", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public long createPerson(@RequestBody @Valid Person person) {
        return personService.createPerson(person.getFirstName(), person.getLastName());
    }

    @RequestMapping(value = "/employees", produces = "application/json", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public long createEmployee(@RequestBody @Valid Employee employee) {
        return personService.createEmployee(employee);
    }

    
    @RequestMapping(value = "/persons/{id:[\\p{Digit}]+}", produces = "application/json", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deletePerson(@PathVariable long id) {
        personService.deletePerson(id);
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String exceptionHandler() {
        return "f^@k";
    }
}
