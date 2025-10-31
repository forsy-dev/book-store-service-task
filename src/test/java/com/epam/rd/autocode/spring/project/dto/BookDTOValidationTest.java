package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private BookDTO.BookDTOBuilder getValidDtoBuilder() {
        return BookDTO.builder()
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
        BookDTO bookDTO = getValidDtoBuilder().build();
        Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
        assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
    }

    @Nested
    class NameValidation {

        @Test
        void whenNameIsBlank_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().name(" ".repeat(3)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenNameIsTooShort_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().name("a".repeat(2)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenNameIsTooLong_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().name("a".repeat(256)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class GenreValidation {

        @Test
        void whenGenreIsBlank_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().genre(" ".repeat(3)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenGenreIsTooShort_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().genre("a".repeat(2)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenGenreIsTooLong_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().genre("a".repeat(256)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class AgeGroupValidation {

        @Test
        void whenAgeGroupIsNull_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().ageGroup(null).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotNull.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class PriceValidation {

        @Test
        void whenPriceIsNull_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().price(null).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotNull.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPriceIsTooSmall_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().price(BigDecimal.ZERO).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{DecimalMin.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class PublicationDateValidation {

        @Test
        void whenPublicationDateIsNull_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().publicationDate(null).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotNull.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPublicationDateIsInFuture_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().publicationDate(LocalDate.now().plusDays(1)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{PastOrPresent.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class AuthorValidation {

        @Test
        void whenAuthorIsBlank_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().author(" ".repeat(3)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenAuthorIsTooShort_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().author("a".repeat(2)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenAuthorIsTooLong_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().author("a".repeat(256)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class PagesValidation {

        @Test
        void whenPagesIsNull_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().pages(null).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotNull.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPagesIsNotPositive_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().pages(0).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{Min.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class CharacteristicsValidation {

        @Test
        void whenCharacteristicsIsBlank_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().characteristics(" ".repeat(3)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenCharacteristicsIsTooShort_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().characteristics("a".repeat(2)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenCharacteristicsIsTooLong_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().characteristics("a".repeat(256)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class DescriptionValidation {

        @Test
        void whenDescriptionIsBlank_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().description(" ".repeat(3)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenDescriptionIsTooShort_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().description("a".repeat(2)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenDescriptionIsTooLong_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().description("a".repeat(2001)).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class LanguageValidation {

        @Test
        void whenLanguageIsNull_thenValidationFails() {
            BookDTO bookDTO = getValidDtoBuilder().language(null).build();
            Set<ConstraintViolation<BookDTO>> violations = validator.validate(bookDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotNull.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }
}
