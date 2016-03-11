package com.hp.example.repositories;

import com.hp.example.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByFirstNameLike(String firstName);

    List<Person> findByLastNameLike(String lastName);
}
