package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "EMPLOYEES")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Employee extends User {

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    public Employee(Long id, String name, String email, String password, String phone, LocalDate birthDate) {
        super(id, name, email, password);
        this.phone = phone;
        this.birthDate = birthDate;
    }
}
