package com.forsy.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.forsy.model.enums.AgeGroup;
import com.forsy.model.enums.Language;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import util.ValidationUtil;

class BookDtoValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  private BookDto.BookDtoBuilder getValidDtoBuilder() {
    return BookDto.builder()
        .name("A Valid Book Name")
        .genre("Science Fiction")
        .ageGroup(AgeGroup.ADULT)
        .price(new BigDecimal("19.99"))
        .publicationDate(LocalDate.now().minusYears(1))
        .author("John Doe")
        .pages(300)
        .characteristics("Hardcover, Illustrated")
        .description("An exciting tale of space adventure.")
        .language(Language.ENGLISH);
  }

  @Test
  void whenAllFieldsAreValid_thenValidationSucceeds() {
    BookDto bookDto = getValidDtoBuilder().build();
    Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);
    assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
  }

  @Nested
  class NameValidation {

    static Stream<Arguments> invalidNameProvider() {
      return Stream.of(
          Arguments.of(" ".repeat(3), Set.of("{NotBlank.invalid}")), // Blank
          Arguments.of("a".repeat(2), Set.of("{Size.invalid}")), // Too Short
          Arguments.of("a".repeat(256), Set.of("{Size.invalid}")), // Too Long
          Arguments.of(null, Set.of("{NotBlank.invalid}")) // Null
      );
    }

    @ParameterizedTest(name = "Name \"{0}\" should trigger {1}")
    @MethodSource("invalidNameProvider")
    void whenNameIsInvalid_thenValidationFails(String name, Set<String> expectedMessages) {
      BookDto bookDto = getValidDtoBuilder().name(name).build();
      Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);
      ValidationUtil.validateMessageTemplates(violations, expectedMessages);
    }
  }

  @Nested
  class GenreValidation {

    static Stream<Arguments> invalidGenreProvider() {
      return Stream.of(
          Arguments.of(" ".repeat(3), Set.of("{NotBlank.invalid}")), // Blank
          Arguments.of("a".repeat(2), Set.of("{Size.invalid}")), // Too Short
          Arguments.of("a".repeat(256), Set.of("{Size.invalid}")), // Too Long
          Arguments.of(null, Set.of("{NotBlank.invalid}")) // Null
      );
    }

    @ParameterizedTest(name = "Genre \"{0}\" should trigger {1}")
    @MethodSource("invalidGenreProvider")
    void whenGenreIsInvalid_thenValidationFails(String genre, Set<String> expectedMessages) {
      BookDto bookDto = getValidDtoBuilder().genre(genre).build();
      Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);
      ValidationUtil.validateMessageTemplates(violations, expectedMessages);
    }
  }

  @Nested
  class AgeGroupValidation {

    @Test
    void whenAgeGroupIsNull_thenValidationFails() {
      BookDto bookDto = getValidDtoBuilder().ageGroup(null).build();
      Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);
      ValidationUtil.validateMessageTemplates(violations, Set.of("{NotNull.invalid}"));
    }
  }

  @Nested
  class PriceValidation {

    static Stream<Arguments> invalidPriceProvider() {
      return Stream.of(
          Arguments.of(null, Set.of("{NotNull.invalid}")), // Null
          Arguments.of(BigDecimal.ZERO, Set.of("{DecimalMin.invalid}")) // Too Small
      );
    }

    @ParameterizedTest(name = "Price \"{0}\" should trigger {1}")
    @MethodSource("invalidPriceProvider")
    void whenPriceIsInvalid_thenValidationFails(BigDecimal price, Set<String> expectedMessages) {
      BookDto bookDto = getValidDtoBuilder().price(price).build();
      Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);
      ValidationUtil.validateMessageTemplates(violations, expectedMessages);
    }
  }

  @Nested
  class PublicationDateValidation {

    static Stream<Arguments> invalidPublicationDateProvider() {
      return Stream.of(
          Arguments.of(null, Set.of("{NotNull.invalid}")), // Null
          Arguments.of(LocalDate.now().plusDays(1), Set.of("{PastOrPresent.invalid}"))
      );
    }

    @ParameterizedTest(name = "Publication Date \"{0}\" should trigger {1}")
    @MethodSource("invalidPublicationDateProvider")
    void whenPublicationDateIsInvalid_thenValidationFails(LocalDate publicationDate,
        Set<String> expectedMessages) {
      BookDto bookDto = getValidDtoBuilder().publicationDate(publicationDate).build();
      Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);
      ValidationUtil.validateMessageTemplates(violations, expectedMessages);
    }
  }

  @Nested
  class AuthorValidation {

    static Stream<Arguments> invalidAuthorProvider() {
      return Stream.of(
          Arguments.of(null, Set.of("{NotBlank.invalid}")), // Null
          Arguments.of(" ".repeat(3), Set.of("{NotBlank.invalid}")), // Blank
          Arguments.of("a".repeat(2), Set.of("{Size.invalid}")), // Too Short
          Arguments.of("a".repeat(256), Set.of("{Size.invalid}")) // Too Long
      );
    }

    @ParameterizedTest(name = "Author \"{0}\" should trigger {1}")
    @MethodSource("invalidAuthorProvider")
    void whenAuthorIsInvalid_thenValidationFails(String author, Set<String> expectedMessages) {
      BookDto bookDto = getValidDtoBuilder().author(author).build();
      Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);
      ValidationUtil.validateMessageTemplates(violations, expectedMessages);
    }
  }

  @Nested
  class PagesValidation {

    static Stream<Arguments> invalidPagesProvider() {
      return Stream.of(
          Arguments.of(null, Set.of("{NotNull.invalid}")), // Null
          Arguments.of(0, Set.of("{Min.invalid}")) // Not Positive
      );
    }

    @ParameterizedTest(name = "Pages \"{0}\" should trigger {1}")
    @MethodSource("invalidPagesProvider")
    void whenPagesIsInvalid_thenValidationFails(Integer pages, Set<String> expectedMessages) {
      BookDto bookDto = getValidDtoBuilder().pages(pages).build();
      Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);
      ValidationUtil.validateMessageTemplates(violations, expectedMessages);
    }
  }

  @Nested
  class CharacteristicsValidation {

    static Stream<Arguments> invalidCharacteristicsProvider() {
      return Stream.of(
          Arguments.of(null, Set.of("{NotBlank.invalid}")), // Null
          Arguments.of(" ".repeat(3), Set.of("{NotBlank.invalid}")), // Blank
          Arguments.of("a".repeat(2), Set.of("{Size.invalid}")), // Too Short
          Arguments.of("a".repeat(256), Set.of("{Size.invalid}")) // Too Long
      );
    }

    @ParameterizedTest(name = "Characteristics \"{0}\" should trigger {1}")
    @MethodSource("invalidCharacteristicsProvider")
    void whenCharacteristicsIsInvalid_thenValidationFails(String characteristics,
        Set<String> expectedMessages) {
      BookDto bookDto = getValidDtoBuilder().characteristics(characteristics).build();
      Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);
      ValidationUtil.validateMessageTemplates(violations, expectedMessages);
    }
  }

  @Nested
  class DescriptionValidation {

    static Stream<Arguments> invalidDescriptionProvider() {
      return Stream.of(
          Arguments.of(null, Set.of("{NotBlank.invalid}")), // Null
          Arguments.of(" ".repeat(3), Set.of("{NotBlank.invalid}")), // Blank
          Arguments.of("a".repeat(2), Set.of("{Size.invalid}")), // Too Short
          Arguments.of("a".repeat(2001), Set.of("{Size.invalid}")) // Too Long
      );
    }

    @ParameterizedTest(name = "Description \"{0}\" should trigger {1}")
    @MethodSource("invalidDescriptionProvider")
    void whenDescriptionIsInvalid_thenValidationFails(String description,
        Set<String> expectedMessages) {
      BookDto bookDto = getValidDtoBuilder().description(description).build();
      Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);
      ValidationUtil.validateMessageTemplates(violations, expectedMessages);
    }
  }

  @Nested
  class LanguageValidation {

    @Test
    void whenLanguageIsNull_thenValidationFails() {
      BookDto bookDto = getValidDtoBuilder().language(null).build();
      Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);
      ValidationUtil.validateMessageTemplates(violations, Set.of("{NotNull.invalid}"));
    }
  }
}
