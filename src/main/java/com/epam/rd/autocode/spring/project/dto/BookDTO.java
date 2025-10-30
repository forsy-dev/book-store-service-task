package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDTO{

    @NotBlank(message = "{NotBlank.Book.name}")
    @Size(min = 3, max = 255, message = "{Size.Book.name}")
    private String name;

    @NotBlank(message = "{NotBlank.Book.genre}")
    @Size(min = 3, max = 255, message = "{Size.Book.genre}")
    private String genre;

    @NotNull(message = "{NotNull.Book.ageGroup}")
    private AgeGroup ageGroup;

    @NotNull(message = "{NotNull.Book.price}")
    @DecimalMin(value = "0.01", message = "{DecimalMin.Book.price}")
    private BigDecimal price;

    @NotNull(message = "{NotNull.Book.publicationDate}")
    @PastOrPresent(message = "{PastOrPresent.Book.publicationDate}")
    private LocalDate publicationDate;

    @NotBlank(message = "{NotBlank.Book.author}")
    @Size(min = 3, max = 255, message = "{Size.Book.author}")
    private String author;

    @NotNull(message = "{NotNull.Book.pages}")
    @Min(value = 1, message = "{Min.Book.pages}")
    private Integer pages;

    @NotBlank(message = "{NotBlank.Book.characteristics}")
    @Size(min = 3, max = 255, message = "{Size.Book.characteristics}")
    private String characteristics;

    @NotBlank(message = "{NotBlank.Book.description}")
    @Size(min = 3, max = 2000, message = "{Size.Book.description}")
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "{NotNull.Book.language}")
    private Language language;
}
