package com.forsy.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

    @Test
    void whenPasswordIsNull_thenValidationFails() {
      ClientCreateDto clientCreateDto = getValidDtoBuilder().password(null).build();
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);
      assertEquals(1, violations.size());
      assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPasswordIsTooShort_thenValidationFails() {
      ClientCreateDto clientCreateDto = getValidDtoBuilder().password("a").build();
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);
      assertEquals(1, violations.size());
      assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPasswordIsTooLong_thenValidationFails() {
      ClientCreateDto clientCreateDto = getValidDtoBuilder()
          .password("Te$t123" + "a".repeat(100)).build();
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);
      assertEquals(1, violations.size());
      assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPasswordDoesNotHaveLowerCharacter_thenValidationFails() {
      ClientCreateDto clientCreateDto = getValidDtoBuilder().password("TE$T1234").build();
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);
      assertEquals(1, violations.size());
      assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPasswordDoesNotHaveUpperCharacter_thenValidationFails() {
      ClientCreateDto clientCreateDto = getValidDtoBuilder().password("te$t1234").build();
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);
      assertEquals(1, violations.size());
      assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPasswordDoesNotHaveDigitCharacter_thenValidationFails() {
      ClientCreateDto clientCreateDto = getValidDtoBuilder().password("Te$ttttt").build();
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);
      assertEquals(1, violations.size());
      assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenPasswordDoesNotHaveSpecialCharacter_thenValidationFails() {
      ClientCreateDto clientCreateDto = getValidDtoBuilder().password("Test1234").build();
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);
      assertEquals(1, violations.size());
      assertEquals("{Pattern.password}", violations.iterator().next().getMessageTemplate());
    }
  }

  @Nested
  class NameValidation {

    @Test
    void whenNameIsBlank_thenValidationFails() {
      ClientCreateDto clientCreateDto = getValidDtoBuilder().name(" ".repeat(3)).build();
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);
      assertEquals(1, violations.size());
      assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenNameIsTooShort_thenValidationFails() {
      ClientCreateDto clientCreateDto = getValidDtoBuilder().name("a".repeat(2)).build();
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);
      assertEquals(1, violations.size());
      assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenNameIsTooLong_thenValidationFails() {
      ClientCreateDto clientCreateDto = getValidDtoBuilder().name("a".repeat(256)).build();
      Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(clientCreateDto);
      assertEquals(1, violations.size());
      assertEquals("{Size.invalid}", violations.iterator().next().getMessageTemplate());
    }
  }
}
