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

public class ClientCreatDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ClientCreatDTO.ClientCreatDTOBuilder getValidDtoBuilder() {
        return ClientCreatDTO.builder()
                .email("test@test.com")
                .password("Te$st1234")
                .name("employee");
    }

    @Test
    void whenAllFieldsAreValid_thenValidationSucceeds() {
        ClientCreatDTO clientCreatDTO = getValidDtoBuilder().build();
        Set<ConstraintViolation<ClientCreatDTO>> violations = validator.validate(clientCreatDTO);
        assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
    }

    @Nested
    class EmailValidation {

        @Test
        void whenEmailIsNull_thenValidationFails() {
            ClientCreatDTO clientCreatDTO = getValidDtoBuilder().email(null).build();
            Set<ConstraintViolation<ClientCreatDTO>> violations = validator.validate(clientCreatDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenEmailIsInvalid_thenValidationFails() {
            ClientCreatDTO clientCreatDTO = getValidDtoBuilder().email("aa").build();
            Set<ConstraintViolation<ClientCreatDTO>> violations = validator.validate(clientCreatDTO);
            assertEquals(1, violations.size());
            assertEquals("{Email.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class PasswordValidation {

        @Test
        void whenPasswordIsNull_thenValidationFails() {
            ClientCreatDTO clientCreatDTO = getValidDtoBuilder().password(null).build();
            Set<ConstraintViolation<ClientCreatDTO>> violations = validator.validate(clientCreatDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordIsTooShort_thenValidationFails() {
            ClientCreatDTO clientCreatDTO = getValidDtoBuilder().password("a").build();
            Set<ConstraintViolation<ClientCreatDTO>> violations = validator.validate(clientCreatDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordIsTooLong_thenValidationFails() {
            ClientCreatDTO clientCreatDTO = getValidDtoBuilder().password("Te$t123" + "a".repeat(100)).build();
            Set<ConstraintViolation<ClientCreatDTO>> violations = validator.validate(clientCreatDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordDoesNotHaveLowerCharacter_thenValidationFails() {
            ClientCreatDTO clientCreatDTO = getValidDtoBuilder().password("TE$T1234").build();
            Set<ConstraintViolation<ClientCreatDTO>> violations = validator.validate(clientCreatDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordDoesNotHaveUpperCharacter_thenValidationFails() {
            ClientCreatDTO clientCreatDTO = getValidDtoBuilder().password("te$t1234").build();
            Set<ConstraintViolation<ClientCreatDTO>> violations = validator.validate(clientCreatDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordDoesNotHaveDigitCharacter_thenValidationFails() {
            ClientCreatDTO clientCreatDTO = getValidDtoBuilder().password("Te$ttttt").build();
            Set<ConstraintViolation<ClientCreatDTO>> violations = validator.validate(clientCreatDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordDoesNotHaveSpecialCharacter_thenValidationFails() {
            ClientCreatDTO clientCreatDTO = getValidDtoBuilder().password("Test1234").build();
            Set<ConstraintViolation<ClientCreatDTO>> violations = validator.validate(clientCreatDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class NameValidation {

        @Test
        void whenNameIsBlank_thenValidationFails() {
            ClientCreatDTO clientCreatDTO = getValidDtoBuilder().name(" ".repeat(3)).build();
            Set<ConstraintViolation<ClientCreatDTO>> violations = validator.validate(clientCreatDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenNameIsTooShort_thenValidationFails() {
            ClientCreatDTO clientCreatDTO = getValidDtoBuilder().name("a".repeat(2)).build();
            Set<ConstraintViolation<ClientCreatDTO>> violations = validator.validate(clientCreatDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenNameIsTooLong_thenValidationFails() {
            ClientCreatDTO clientCreatDTO = getValidDtoBuilder().name("a".repeat(256)).build();
            Set<ConstraintViolation<ClientCreatDTO>> violations = validator.validate(clientCreatDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }
}
