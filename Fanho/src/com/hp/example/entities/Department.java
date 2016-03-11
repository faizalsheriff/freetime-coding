package com.hp.example.entities;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import javax.persistence.Table;


@Entity
@Table(name="DEPARTMENT")
public class Department {
 
    @Id
    @GeneratedValue
    @Column(name="DEPARTMENT_ID")
    private Long departmentId;
     
    @Column(name="DEPT_NAME")
    private String departmentName;
     
    @OneToMany(mappedBy="department")
    private Set<Employee> employeess;
 
}   // Getter and Setter methods