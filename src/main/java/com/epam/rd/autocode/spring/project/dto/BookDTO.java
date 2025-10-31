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

    @NotBlank(message = "{NotBlank.invalid}")
    @Size(min = 3, max = 255, message = "{Size.invalid}")
    private String name;

    @NotBlank(message = "{NotBlank.invalid}")
    @Size(min = 3, max = 255, message = "{Size.invalid}")
    private String genre;

    @NotNull(message = "{NotNull.invalid}")
    private AgeGroup ageGroup;

    @NotNull(message = "{NotNull.invalid}")
    @DecimalMin(value = "0.01", message = "{DecimalMin.invalid}")
    private BigDecimal price;

    @NotNull(message = "{NotNull.invalid}")
    @PastOrPresent(message = "{PastOrPresent.invalid}")
    private LocalDate publicationDate;

    @NotBlank(message = "{NotBlank.invalid}")
    @Size(min = 3, max = 255, message = "{Size.invalid}")
    private String author;

    @NotNull(message = "{NotNull.invalid}")
    @Min(value = 1, message = "{Min.invalid}")
    private Integer pages;

    @NotBlank(message = "{NotBlank.invalid}")
    @Size(min = 3, max = 255, message = "{Size.invalid}")
    private String characteristics;

    @NotBlank(message = "{NotBlank.invalid}")
    @Size(min = 3, max = 2000, message = "{Size.invalid}")
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "{NotNull.invalid}")
    private Language language;
}
