package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddToCartDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private AddToCartDTO.AddToCartDTOBuilder getValidDtoBuilder() {
        return AddToCartDTO.builder()
                .bookName("book")
                .quantity(1);
    }

    @Test
    void whenAllFieldsAreValid_thenValidationSucceeds() {
        AddToCartDTO addToCartDTO = getValidDtoBuilder().build();
        Set<ConstraintViolation<AddToCartDTO>> violations = validator.validate(addToCartDTO);
        assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
    }

    @Nested
    class BookNameValidation {

        @Test
        void whenBookNameIsNull_thenValidationFails() {
            AddToCartDTO addBalanceDTO = getValidDtoBuilder().bookName(" ".repeat(3)).build();
            Set<ConstraintViolation<AddToCartDTO>> violations = validator.validate(addBalanceDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenBookNameIsTooShort_thenValidationFails() {
            AddToCartDTO addBalanceDTO = getValidDtoBuilder().bookName("a".repeat(2)).build();
            Set<ConstraintViolation<AddToCartDTO>> violations = validator.validate(addBalanceDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenBookNameIsTooLong_thenValidationFails() {
            AddToCartDTO addBalanceDTO = getValidDtoBuilder().bookName("a".repeat(256)).build();
            Set<ConstraintViolation<AddToCartDTO>> violations = validator.validate(addBalanceDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class QuantityValidation {

        @Test
        void whenQuantityIsNull_thenValidationFails() {
            AddToCartDTO addBalanceDTO = getValidDtoBuilder().quantity(null).build();
            Set<ConstraintViolation<AddToCartDTO>> violations = validator.validate(addBalanceDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotNull.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenQuantityIsNotPositive_thenValidationFails() {
            AddToCartDTO addBalanceDTO = getValidDtoBuilder().quantity(0).build();
            Set<ConstraintViolation<AddToCartDTO>> violations = validator.validate(addBalanceDTO);
            assertEquals(1, violations.size());
            assertEquals("{Min.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }
}
