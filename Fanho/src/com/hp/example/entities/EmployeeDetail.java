package com.hp.example.entities;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.jws.Oneway;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/*
@Entity
@Table(name="EMPLOYEEDETAIL")*/
public class EmployeeDetail {
     
    @Id
    @Column(name="employee_id", unique=true, nullable=false)
    @GeneratedValue(generator="gen")
    @GenericGenerator(name="gen", strategy="foreign", parameters=@Parameter(name="property", value="employee"))
    private Long employeeId;
     
    @Column(name="street")
    private String street;
 
  
    @Column(name="city")
    private String city;
 
    @Column(name="state")
    private String state;
 
    @Column(name="country")
    private String country;
     
    @OneToOne
    @PrimaryKeyJoinColumn
    private Employee employee;
 
    public EmployeeDetail() {
 
    }
 
    public EmployeeDetail(String street, String city, String state, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
    }
 
    // Getter and Setter methods
}
