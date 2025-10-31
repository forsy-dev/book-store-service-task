package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChangePasswordDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ChangePasswordDTO.ChangePasswordDTOBuilder getValidDtoBuilder() {
        return ChangePasswordDTO.builder()
                .oldPassword("test1234")
                .newPassword("Te$t1234");
    }

    @Test
    void whenAllFieldsAreValid_thenValidationSucceeds() {
        ChangePasswordDTO changePasswordDTO = getValidDtoBuilder().build();
        Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(changePasswordDTO);
        assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
    }

    @Nested
    class OldPasswordValidation {

        @Test
        void whenOldPasswordIsBlank_thenValidationFails() {
            ChangePasswordDTO changePasswordDTO = getValidDtoBuilder().oldPassword(" ").build();
            Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(changePasswordDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class NewPasswordValidation {

        @Test
        void whenNewPasswordIsNull_thenValidationFails() {
            ChangePasswordDTO changePasswordDTO = getValidDtoBuilder().newPassword(null).build();
            Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(changePasswordDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordIsTooShort_thenValidationFails() {
            ChangePasswordDTO changePasswordDTO = getValidDtoBuilder().newPassword("a").build();
            Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(changePasswordDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordIsTooLong_thenValidationFails() {
            ChangePasswordDTO changePasswordDTO = getValidDtoBuilder().newPassword("Te$t123" + "a".repeat(100)).build();
            Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(changePasswordDTO);
            assertEquals(1, violations.size());
            assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordDoesNotHaveLowerCharacter_thenValidationFails() {
            ChangePasswordDTO changePasswordDTO = getValidDtoBuilder().newPassword("TE$T1234").build();
            Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(changePasswordDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordDoesNotHaveUpperCharacter_thenValidationFails() {
            ChangePasswordDTO changePasswordDTO = getValidDtoBuilder().newPassword("te$t1234").build();
            Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(changePasswordDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordDoesNotHaveDigitCharacter_thenValidationFails() {
            ChangePasswordDTO changePasswordDTO = getValidDtoBuilder().newPassword("Te$ttttt").build();
            Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(changePasswordDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenPasswordDoesNotHaveSpecialCharacter_thenValidationFails() {
            ChangePasswordDTO changePasswordDTO = getValidDtoBuilder().newPassword("Test1234").build();
            Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(changePasswordDTO);
            assertEquals(1, violations.size());
            assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
        }
    }
}
