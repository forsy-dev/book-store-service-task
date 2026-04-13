package com.forsy.model;

import com.forsy.model.enums.AgeGroup;
import com.forsy.model.enums.Language;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
 * Persistent entity representing a book within the bookstore's inventory.
 *
 * <p>This class serves as the primary domain model and database entity,
 * capturing all essential metadata for a literary work. It defines the
 * schema for the "BOOKS" table and enforces data integrity through both
 * JPA constraints and Bean Validation annotations.
 *
 * @author Illia
 */
@Entity
@Table(name = "BOOKS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

  /**
   * The unique primary key for the book entity.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The official title of the book.
   *
   * <p>Must be unique across the system and between 3 and 255 characters.
   */
  @Column(name = "NAME", nullable = false, unique = true)
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 255, message = "{Size.invalid}")
  private String name;

  /**
   * The literary classification or category of the book (e.g., Thriller, Sci-Fi).
   */
  @Column(name = "GENRE", nullable = false)
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 255, message = "{Size.invalid}")
  private String genre;

  /**
   * The target audience age group for the book.
   *
   * <p>Stored as a string representation of the {@link AgeGroup} enum.
   */
  @Column(name = "AGE_GROUP", nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull(message = "{NotNull.invalid}")
  private AgeGroup ageGroup;

  /**
   * The retail price of the book.
   *
   * <p>Must be a positive value of at least 0.01.
   */
  @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
  @NotNull(message = "{NotNull.invalid}")
  @DecimalMin(value = "0.01", message = "{DecimalMin.invalid}")
  private BigDecimal price;

  /**
   * The date the book was officially published.
   *
   * <p>Cannot be a future date.
   */
  @Column(name = "PUBLICATION_DATE", nullable = false)
  @NotNull(message = "{NotNull.invalid}")
  @PastOrPresent(message = "{PastOrPresent.invalid}")
  private LocalDate publicationDate;

  /**
   * The name of the individual or entity that authored the book.
   */
  @Column(name = "AUTHOR", nullable = false)
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 255, message = "{Size.invalid}")
  private String author;

  /**
   * The total page count of the book's physical or digital edition.
   */
  @Column(name = "NUMBER_OF_PAGES", nullable = false)
  @NotNull(message = "{NotNull.invalid}")
  @Min(value = 1, message = "{Min.invalid}")
  private Integer pages;

  /**
   * Technical or physical attributes of the book (e.g., binding type, paper weight).
   */
  @Column(name = "CHARACTERISTICS", nullable = false)
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 255, message = "{Size.invalid}")
  private String characteristics;

  /**
   * A comprehensive summary or marketing blurb describing the book's content.
   *
   * <p>Mapped to a large text column in the database to accommodate detailed entries.
   */
  @Column(name = "DESCRIPTION", nullable = false, columnDefinition = "TEXT")
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 2000, message = "{Size.invalid}")
  private String description;

  /**
   * The primary language in which the book is written.
   *
   * <p>Stored as a string representation of the {@link Language} enum.
   */
  @Column(name = "LANGUAGE", nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull(message = "{NotNull.invalid}")
  private Language language;
}
