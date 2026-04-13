package com.forsy.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class EmployeeDtoValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  private EmployeeDto.EmployeeDtoBuilder getValidDtoBuilder() {
    return EmployeeDto.builder()
        .email("test@test.com")
        .password("Te$st1234")
        .name("employee")
        .birthDate(LocalDate.now().minusYears(18))
        .phone("1234567890");
  }

  @Test
  void whenAllFieldsAreValid_thenValidationSucceeds() {
    EmployeeDto employeeDTO = getValidDtoBuilder().build();
    Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
    assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
  }

  @Nested
  class EmailValidation {

    @Test
    void whenEmailIsNull_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().email(null).build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenEmailIsInvalid_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().email("aa").build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{Email.invalid}", violations.iterator().next().getMessageTemplate());
    }
  }

  @Nested
  class PasswordValidation {

    @Test
    void whenPasswordIsNull_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().password(null).build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPasswordIsTooShort_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().password("a").build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPasswordIsTooLong_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().password("Te$t123" + "a".repeat(100)).build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPasswordDoesNotHaveLowerCharacter_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().password("TE$T1234").build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPasswordDoesNotHaveUpperCharacter_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().password("te$t1234").build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPasswordDoesNotHaveDigitCharacter_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().password("Te$ttttt").build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPasswordDoesNotHaveSpecialCharacter_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().password("Test1234").build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
    }
  }

  @Nested
  class NameValidation {

    @Test
    void whenNameIsBlank_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().name(" ".repeat(3)).build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenNameIsTooShort_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().name("a".repeat(2)).build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenNameIsTooLong_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().name("a".repeat(256)).build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
    }
  }

  @Nested
  class BirthDateValidation {

    @Test
    void whenBirthDateIsNull_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().birthDate(null).build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{NotNull.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenBirthDateIsInFuture_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().birthDate(LocalDate.now().plusDays(1)).build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{PastOrPresent.invalid}", violations.iterator().next().getMessageTemplate());
    }
  }

  @Nested
  class PhoneValidation {

    @Test
    void whenPhoneIsNull_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().phone(null).build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPhoneIsTooShort_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().phone("1").build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{Pattern.phone}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPhoneIsTooLong_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().phone("1".repeat(21)).build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{Pattern.phone}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPhoneHasInvalidCharacters_thenValidationFails() {
      EmployeeDto employeeDTO = getValidDtoBuilder().phone("1234*56789").build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDTO);
      assertEquals(1, violations.size());
      assertEquals("{Pattern.phone}", violations.iterator().next().getMessageTemplate());
    }
  }
}
