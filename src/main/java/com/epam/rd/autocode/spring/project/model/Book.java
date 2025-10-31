package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "BOOKS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true)
    @NotBlank(message = "{NotBlank.invalid}")
    @Size(min = 3, max = 255, message = "{Size.invalid}")
    private String name;

    @Column(name = "GENRE", nullable = false)
    @NotBlank(message = "{NotBlank.invalid}")
    @Size(min = 3, max = 255, message = "{Size.invalid}")
    private String genre;

    @Column(name = "AGE_GROUP", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "{NotNull.invalid}")
    private AgeGroup ageGroup;

    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "{NotNull.invalid}")
    @DecimalMin(value = "0.01", message = "{DecimalMin.invalid}")
    private BigDecimal price;

    @Column(name = "PUBLICATION_DATE", nullable = false)
    @NotNull(message = "{NotNull.invalid}")
    @PastOrPresent(message = "{PastOrPresent.invalid}")
    private LocalDate publicationDate;

    @Column(name = "AUTHOR", nullable = false)
    @NotBlank(message = "{NotBlank.invalid}")
    @Size(min = 3, max = 255, message = "{Size.invalid}")
    private String author;

    @Column(name = "NUMBER_OF_PAGES", nullable = false)
    @NotNull(message = "{NotNull.invalid}")
    @Min(value = 1, message = "{Min.invalid}")
    private Integer pages;

    @Column(name = "CHARACTERISTICS", nullable = false)
    @NotBlank(message = "{NotBlank.invalid}")
    @Size(min = 3, max = 255, message = "{Size.invalid}")
    private String characteristics;

    @Column(name = "DESCRIPTION", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "{NotBlank.invalid}")
    @Size(min = 3, max = 2000, message = "{Size.invalid}")
    private String description;

    @Column(name = "LANGUAGE", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "{NotNull.invalid}")
    private Language language;
}
