package com.forsy.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ClientCreateDtoValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  private ClientCreateDto.ClientCreateDtoBuilder getValidDtoBuilder() {
    return ClientCreateDto.builder()
        .email("test@test.com")
        .password("Te$st1234")
        .name("employee");
  }

  @Test
  void whenAllFieldsAreValid_thenValidationSucceeds() {
    ClientCreateDto clientCreateDto = getValidDtoBuilder().build();
    Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);
    assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
  }

  @Nested
  class EmailValidation {

    @Test
    void whenEmailIsNull_thenValidationFails() {
      ClientCreateDto clientCreateDto = getValidDtoBuilder().email(null).build();
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);
      assertEquals(1, violations.size());
      assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenEmailIsInvalid_thenValidationFails() {
      ClientCreateDto clientCreateDto = getValidDtoBuilder().email("aa").build();
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);
      assertEquals(1, violations.size());
      assertEquals("{Email.invalid}", violations.iterator().next().getMessageTemplate());
    }
  }

  @Nested
  class PasswordValidation {

    @ParameterizedTest(name = "Password [{0}] should fail with template: {1}")
    @MethodSource("invalidPasswordProvider")
    void whenPasswordIsInvalid_thenValidationFails(String password, String expectedTemplate) {
      // 1. Arrange
      ClientCreateDto clientCreateDto = getValidDtoBuilder().password(password).build();

      // 2. Act
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);

      // 3. Assert
      assertEquals(1, violations.size(), "Should have exactly 1 validation constraint failure");
      assertEquals(expectedTemplate, violations.iterator().next().getMessageTemplate());
    }

    // This data provider feeds your test scenarios cleanly
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
      // Arrange
      ClientCreateDto clientCreateDto = getValidDtoBuilder().name(name).build();

      // Act
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);

      // Assert
      assertEquals(1, violations.size(), "Should trigger exactly one validation failure");
      assertEquals(expectedTemplate, violations.iterator().next().getMessageTemplate());
    }

    private static Stream<Arguments> invalidNameProvider() {
      return Stream.of(
          Arguments.of(" ".repeat(3), "{NotBlank.invalid}"), // Blank name
          Arguments.of("a".repeat(2), "{Size.invalid}"),     // Too short
          Arguments.of("a".repeat(256), "{Size.invalid}")    // Too long
      );
    }
  }
}
