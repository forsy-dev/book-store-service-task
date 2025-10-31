package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "EMPLOYEES")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Employee extends User {

    @Column(name = "PHONE")
    @NotBlank(message = "{NotBlank.invalid}")
    @Pattern(regexp = "^\\+?[0-9\\s\\(\\)-]{10,20}$", message = "{Pattern.phone}")
    private String phone;

    @Column(name = "BIRTH_DATE")
    @NotNull(message = "{NotNull.invalid}")
    @PastOrPresent(message = "{PastOrPresent.invalid}")
    private LocalDate birthDate;

    public Employee(Long id, String name, String email, String password, String phone, LocalDate birthDate) {
        super(id, name, email, password);
        this.phone = phone;
        this.birthDate = birthDate;
    }
}
