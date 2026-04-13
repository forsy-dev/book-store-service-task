package com.forsy.dto;

import com.forsy.model.enums.AgeGroup;
import com.forsy.model.enums.Language;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing a book's full details.
 *
 * <p>This comprehensive DTO is used to transport book data between the service layer
 * and the web interface. It includes all metadata necessary for displaying book
 * details, as well as validation constraints for creating or updating book records
 * in the bookstore's catalog.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto {

  /**
   * The primary title or name of the book.
   *
   * <p>Must be between 3 and 255 characters.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 255, message = "{Size.invalid}")
  private String name;

  /**
   * The literary category or genre of the book.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 255, message = "{Size.invalid}")
  private String genre;

  /**
   * The target audience age group for the book.
   */
  @NotNull(message = "{NotNull.invalid}")
  private AgeGroup ageGroup;

  /**
   * The retail price of the book in USD.
   *
   * <p>Must be a positive value of at least 0.01.
   */
  @NotNull(message = "{NotNull.invalid}")
  @DecimalMin(value = "0.01", message = "{DecimalMin.invalid}")
  private BigDecimal price;

  /**
   * The date the book was officially published.
   *
   * <p>Cannot be a future date.
   */
  @NotNull(message = "{NotNull.invalid}")
  @PastOrPresent(message = "{PastOrPresent.invalid}")
  private LocalDate publicationDate;

  /**
   * The name of the individual or entity that authored the book.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 255, message = "{Size.invalid}")
  private String author;

  /**
   * The total count of pages in the physical or digital edition.
   *
   * <p>Must be at least 1.
   */
  @NotNull(message = "{NotNull.invalid}")
  @Min(value = 1, message = "{Min.invalid}")
  private Integer pages;

  /**
   * A summary of physical or technical characteristics (e.g., binding, paper type).
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 255, message = "{Size.invalid}")
  private String characteristics;

  /**
   * A detailed synopsis or marketing description of the book's content.
   *
   * <p>Supports up to 2000 characters for descriptive text.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 2000, message = "{Size.invalid}")
  private String description;

  /**
   * The language in which the book is written.
   */
  @Enumerated(EnumType.STRING)
  @NotNull(message = "{NotNull.invalid}")
  private Language language;
}
