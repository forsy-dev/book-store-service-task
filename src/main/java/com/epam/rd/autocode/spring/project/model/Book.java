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
    @NotBlank(message = "{NotBlank.Book.name}")
    @Size(min = 3, max = 255, message = "{Size.Book.name}")
    private String name;

    @Column(name = "GENRE", nullable = false)
    @NotBlank(message = "{NotBlank.Book.genre}")
    @Size(min = 3, max = 255, message = "{Size.Book.genre}")
    private String genre;

    @Column(name = "AGE_GROUP", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "{NotNull.Book.ageGroup}")
    private AgeGroup ageGroup;

    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "{NotNull.Book.price}")
    @DecimalMin(value = "0.01", message = "{DecimalMin.Book.price}")
    private BigDecimal price;

    @Column(name = "PUBLICATION_DATE", nullable = false)
    @NotNull(message = "{NotNull.Book.publicationDate}")
    @PastOrPresent(message = "{PastOrPresent.Book.publicationDate}")
    private LocalDate publicationDate;

    @Column(name = "AUTHOR", nullable = false)
    @NotBlank(message = "{NotBlank.Book.author}")
    @Size(min = 3, max = 255, message = "{Size.Book.author}")
    private String author;

    @Column(name = "NUMBER_OF_PAGES", nullable = false)
    @NotNull(message = "{NotNull.Book.pages}")
    @Min(value = 1, message = "{Min.Book.pages}")
    private Integer pages;

    @Column(name = "CHARACTERISTICS", nullable = false)
    @NotBlank(message = "{NotBlank.Book.characteristics}")
    @Size(min = 3, max = 255, message = "{Size.Book.characteristics}")
    private String characteristics;

    @Column(name = "DESCRIPTION", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "{NotBlank.Book.description}")
    @Size(min = 3, max = 2000, message = "{Size.Book.description}")
    private String description;

    @Column(name = "LANGUAGE", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "{NotNull.Book.language}")
    private Language language;
}
