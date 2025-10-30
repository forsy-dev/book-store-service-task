package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "BOOKS")
@Data
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "GENRE")
    private String genre;

    @Column(name = "AGE_GROUP")
    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;

    @Column(name = "PRICE")
    private BigDecimal price;

    @Column(name = "PUBLICATION_DATE")
    private LocalDate publicationDate;

    @Column(name = "AUTHOR")
    private String author;

    @Column(name = "NUMBER_OF_PAGES")
    private Integer numberOfPages;

    @Column(name = "CHARACTERISTICS")
    private String characteristics;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "LANGUAGE")
    @Enumerated(EnumType.STRING)
    private Language language;
}
