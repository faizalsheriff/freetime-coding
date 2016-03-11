package com.hp.example.services;

import com.hp.example.entities.Employee;
import com.hp.example.entities.Person;
import com.hp.example.repositories.EmployeeRepository;
import com.hp.example.repositories.PersonRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// the service is final because there's no reason to ever extend it.Polymorphism should only be used in the model
@Service
public final class PersonServiceImpl implements PersonService {
    @Autowired
    private PersonRepository personRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)    // notice the read only flag - this is an important optimization for read methods
    public List<Person> readAllPersons() {
        return personRepository.findAll();
    }

    @Override
    @Transactional
    public long createPerson(String firstName, String lastName) {
        return personRepository.save(new Person(firstName, lastName)).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Person readPerson(long id) {
        return personRepository.findOne(id);
    }

    @Override
    @Transactional
    public void deletePerson(long id) {
        personRepository.delete(id);
    }
    
    
   
    @Transactional
    public long createEmployee(Employee e) {
    	
        return employeeRepository.save(e).getEmployeeId();
    }
}
