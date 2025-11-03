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

public class ClientCreateDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ClientCreateDTO.ClientCreateDTOBuilder getValidDtoBuilder() {
        return ClientCreateDTO.builder()
                .email("test@test.com")
                .password("Te$st1234")
                .name("employee");
    }

    @Test
    void whenAllFieldsAreValid_thenValidationSucceeds() {
        ClientCreateDTO clientCreateDTO = getValidDtoBuilder().build();
        Set<ConstraintViolation<ClientCreateDTO>> violations = validator.validate(clientCreateDTO);
        assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
    }

    @Nested
    class EmailValidation {

        @Test
        void whenEmailIsNull_thenValidationFails() {
            ClientCreateDTO clientCreateDTO = getValidDtoBuilder().email(null).build();
            Set<ConstraintViolation<ClientCreateDTO>> violations = validator.validate(clientCreateDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenEmailIsInvalid_thenValidationFails() {
            ClientCreateDTO clientCreateDTO = getValidDtoBuilder().email("aa").build();
            Set<ConstraintViolation<ClientCreateDTO>> violations = validator.validate(clientCreateDTO);
            assertEquals(1, violations.size());
            assertEquals("{Email.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class PasswordValidation {

        @Test
        void whenPasswordIsNull_thenValidationFails() {
            ClientCreateDTO clientCreateDTO = getValidDtoBuilder().password(null).build();
            Set<ConstraintViolation<ClientCreateDTO>> violations = validator.validate(clientCreateDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordIsTooShort_thenValidationFails() {
            ClientCreateDTO clientCreateDTO = getValidDtoBuilder().password("a").build();
            Set<ConstraintViolation<ClientCreateDTO>> violations = validator.validate(clientCreateDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordIsTooLong_thenValidationFails() {
            ClientCreateDTO clientCreateDTO = getValidDtoBuilder().password("Te$t123" + "a".repeat(100)).build();
            Set<ConstraintViolation<ClientCreateDTO>> violations = validator.validate(clientCreateDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordDoesNotHaveLowerCharacter_thenValidationFails() {
            ClientCreateDTO clientCreateDTO = getValidDtoBuilder().password("TE$T1234").build();
            Set<ConstraintViolation<ClientCreateDTO>> violations = validator.validate(clientCreateDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordDoesNotHaveUpperCharacter_thenValidationFails() {
            ClientCreateDTO clientCreateDTO = getValidDtoBuilder().password("te$t1234").build();
            Set<ConstraintViolation<ClientCreateDTO>> violations = validator.validate(clientCreateDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordDoesNotHaveDigitCharacter_thenValidationFails() {
            ClientCreateDTO clientCreateDTO = getValidDtoBuilder().password("Te$ttttt").build();
            Set<ConstraintViolation<ClientCreateDTO>> violations = validator.validate(clientCreateDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordDoesNotHaveSpecialCharacter_thenValidationFails() {
            ClientCreateDTO clientCreateDTO = getValidDtoBuilder().password("Test1234").build();
            Set<ConstraintViolation<ClientCreateDTO>> violations = validator.validate(clientCreateDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class NameValidation {

        @Test
        void whenNameIsBlank_thenValidationFails() {
            ClientCreateDTO clientCreateDTO = getValidDtoBuilder().name(" ".repeat(3)).build();
            Set<ConstraintViolation<ClientCreateDTO>> violations = validator.validate(clientCreateDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenNameIsTooShort_thenValidationFails() {
            ClientCreateDTO clientCreateDTO = getValidDtoBuilder().name("a".repeat(2)).build();
            Set<ConstraintViolation<ClientCreateDTO>> violations = validator.validate(clientCreateDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenNameIsTooLong_thenValidationFails() {
            ClientCreateDTO clientCreateDTO = getValidDtoBuilder().name("a".repeat(256)).build();
            Set<ConstraintViolation<ClientCreateDTO>> violations = validator.validate(clientCreateDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }
}
