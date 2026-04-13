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
import util.ValidationUtil;

class ChangePasswordDtoValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  private ChangePasswordDto.ChangePasswordDtoBuilder getValidDtoBuilder() {
    return ChangePasswordDto.builder()
        .oldPassword("test1234")
        .newPassword("Te$t1234");
  }

  @Test
  void whenAllFieldsAreValid_thenValidationSucceeds() {
    ChangePasswordDto changePasswordDTO = getValidDtoBuilder().build();
    Set<ConstraintViolation<ChangePasswordDto>> violations = validator.validate(changePasswordDTO);
    assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
  }

  @Nested
  class OldPasswordValidation {

    @Test
    void whenOldPasswordIsBlank_thenValidationFails() {
      ChangePasswordDto changePasswordDTO = getValidDtoBuilder().oldPassword(" ").build();
      Set<ConstraintViolation<ChangePasswordDto>> violations = validator.validate(changePasswordDTO);
      assertEquals(1, violations.size());
      assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
    }
  }

  @Nested
  class NewPasswordValidation {

    static Stream<Arguments> invalidNewPasswordProvider() {
      return Stream.of(
          Arguments.of(null, Set.of("{NotBlank.invalid}")), // Null
          Arguments.of("a", Set.of("{Pattern.password}")), // Too Short
          Arguments.of("Te$t123" + "a".repeat(100), Set.of("{Size.invalid}")), // Too Long
          Arguments.of("TE$T1234", Set.of("{Pattern.password}")), // Does Not Have Lower Character
          Arguments.of("te$t1234", Set.of("{Pattern.password}")), // Does Not Have Upper Character
          Arguments.of("Te$ttttt", Set.of("{Pattern.password}")), // Does Not Have Digit Character
          Arguments.of("Test1234", Set.of("{Pattern.password}")) // Does Not Have Special Character
      );
    }

    @ParameterizedTest(name = "New Password \"{0}\" should trigger {1}")
    @MethodSource("invalidNewPasswordProvider")
    void whenNewPasswordIsInvalid_thenValidationFails(String newPassword, Set<String> expectedMessages) {
      ChangePasswordDto passwordDTO = getValidDtoBuilder().newPassword(newPassword).build();
      Set<ConstraintViolation<ChangePasswordDto>> violations = validator.validate(passwordDTO);
      ValidationUtil.validateMessageTemplates(violations, expectedMessages);
    }
  }
}
