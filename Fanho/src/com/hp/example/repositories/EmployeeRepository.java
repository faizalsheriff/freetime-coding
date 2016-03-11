package com.hp.example.repositories;

import com.hp.example.entities.Employee;
import com.hp.example.entities.Person;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Person> findByFirstNameLike(String firstName);

    List<Person> findByLastNameLike(String lastName);
}
