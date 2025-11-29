package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false)
    @NotBlank(message = "{NotBlank.invalid}")
    @Size(min = 3, max = 255, message = "{Size.invalid}")
    private String name;

    @Column(name = "EMAIL", nullable = false, unique = true, updatable = false)
    @NotBlank(message = "{NotBlank.invalid}")
    @Email(message = "{Email.invalid}")
    private String email;

    @Column(name = "PASSWORD")
    @NotBlank(message = "{NotBlank.invalid}")
    private String password;
}
