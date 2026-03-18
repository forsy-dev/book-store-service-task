package com.forsy.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import util.ValidationUtil;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddToCartDTOValidationTest {

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

        static Stream<Arguments> invalidBookNameProvider() {
            return Stream.of(
                Arguments.of(" ".repeat(3), Set.of("{NotBlank.invalid}")), // Blank
                Arguments.of("a".repeat(2), Set.of("{Size.invalid}")), // Too Short
                Arguments.of("a".repeat(256), Set.of( "{Size.invalid}")), // Too Long
                Arguments.of(null, Set.of("{NotBlank.invalid}")) // Null
            );
        }

        @ParameterizedTest(name = "Book name \"{0}\" should trigger {1}")
        @MethodSource("invalidBookNameProvider")
        void whenBookNameIsInvalid_thenValidationFails(String bookName, Set<String> expectedMessages) {
            AddToCartDTO addBalanceDTO = getValidDtoBuilder()
                .bookName(bookName)
                .build();

            Set<ConstraintViolation<AddToCartDTO>> violations = validator.validate(addBalanceDTO);
            ValidationUtil.validateMessageTemplates(violations, expectedMessages);
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
