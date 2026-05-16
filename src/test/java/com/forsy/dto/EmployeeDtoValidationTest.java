package com.forsy.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EmployeeDtoValidationTest {

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
    EmployeeDto employeeDto = getValidDtoBuilder().build();
    Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDto);
    assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
  }

  @Nested
  class EmailValidation {

    @Test
    void whenEmailIsNull_thenValidationFails() {
      EmployeeDto employeeDto = getValidDtoBuilder().email(null).build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDto);
      assertEquals(1, violations.size());
      assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenEmailIsInvalid_thenValidationFails() {
      EmployeeDto employeeDto = getValidDtoBuilder().email("aa").build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDto);
      assertEquals(1, violations.size());
      assertEquals("{Email.invalid}", violations.iterator().next().getMessageTemplate());
    }
  }

  @Nested
  class PasswordValidation {

    @ParameterizedTest(name = "Employee Password [{0}] should fail with template: {1}")
    @MethodSource("invalidPasswordProvider")
    void whenPasswordIsInvalid_thenValidationFails(String password, String expectedTemplate) {
      // Arrange
      EmployeeDto employeeDto = getValidDtoBuilder().password(password).build();

      // Act
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDto);

      // Assert
      assertEquals(1, violations.size(), "Should have exactly 1 validation constraint failure");
      assertEquals(expectedTemplate, violations.iterator().next().getMessageTemplate());
    }

    private static Stream<Arguments> invalidPasswordProvider() {
      return Stream.of(
          Arguments.of(null, "{NotBlank.invalid}"),
          Arguments.of("a", "{Pattern.password}"),
          Arguments.of("Te$t123" + "a".repeat(100), "{Size.invalid}"),
          Arguments.of("TE$T1234", "{Pattern.password}"),
          Arguments.of("te$t1234", "{Pattern.password}"),
          Arguments.of("Te$ttttt", "{Pattern.password}"),
          Arguments.of("Test1234", "{Pattern.password}")
      );
    }
  }

  @Nested
  class NameValidation {

    @ParameterizedTest(name = "Name evaluation: [{0}] expects template {1}")
    @MethodSource("invalidNameProvider")
    void whenNameIsInvalid_thenValidationFails(String name, String expectedTemplate) {
      EmployeeDto employeeDto = getValidDtoBuilder().name(name).build();

      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDto);

      assertEquals(1, violations.size());
      assertEquals(expectedTemplate, violations.iterator().next().getMessageTemplate());
    }

    private static Stream<Arguments> invalidNameProvider() {
      return Stream.of(
          Arguments.of(" ".repeat(3), "{NotBlank.invalid}"),
          Arguments.of("a".repeat(2), "{Size.invalid}"),
          Arguments.of("a".repeat(256), "{Size.invalid}")
      );
    }
  }

  @Nested
  class BirthDateValidation {

    @Test
    void whenBirthDateIsNull_thenValidationFails() {
      EmployeeDto employeeDto = getValidDtoBuilder().birthDate(null).build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDto);
      assertEquals(1, violations.size());
      assertEquals("{NotNull.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenBirthDateIsInFuture_thenValidationFails() {
      EmployeeDto employeeDto = getValidDtoBuilder().birthDate(LocalDate.now().plusDays(1)).build();
      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDto);
      assertEquals(1, violations.size());
      assertEquals("{PastOrPresent.invalid}", violations.iterator().next().getMessageTemplate());
    }
  }

  @Nested
  class PhoneValidation {

    @ParameterizedTest(name = "Phone evaluation: [{0}] expects template {1}")
    @MethodSource("invalidPhoneProvider")
    void whenPhoneIsInvalid_thenValidationFails(String phone, String expectedTemplate) {
      EmployeeDto employeeDto = getValidDtoBuilder().phone(phone).build();

      Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDto);

      assertEquals(1, violations.size());
      assertEquals(expectedTemplate, violations.iterator().next().getMessageTemplate());
    }

    private static Stream<Arguments> invalidPhoneProvider() {
      return Stream.of(
          Arguments.of(null, "{NotBlank.invalid}"),
          Arguments.of("1", "{Pattern.phone}"),
          Arguments.of("1".repeat(21), "{Pattern.phone}"),
          Arguments.of("1234*56789", "{Pattern.phone}")
      );
    }
  }
}
