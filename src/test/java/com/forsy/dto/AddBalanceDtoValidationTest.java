package com.forsy.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AddBalanceDtoValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  private AddBalanceDto.AddBalanceDtoBuilder getValidDtoBuilder() {
    return AddBalanceDto.builder()
        .amount(new BigDecimal("19.99"));
  }

  @Test
  void whenAllFieldsAreValid_thenValidationSucceeds() {
    AddBalanceDto addBalanceDto = getValidDtoBuilder().build();
    Set<ConstraintViolation<AddBalanceDto>> violations = validator.validate(addBalanceDto);
    assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
  }

  @Test
  void whenBalanceIsNull_thenValidationFails() {
    AddBalanceDto addBalanceDto = getValidDtoBuilder().amount(null).build();
    Set<ConstraintViolation<AddBalanceDto>> violations = validator.validate(addBalanceDto);
    assertEquals(1, violations.size());
    assertEquals("{NotNull.invalid}", violations.iterator().next().getMessageTemplate());
  }

  @Test
  void whenBalanceIsNotTooSmall_thenValidationFails() {
    AddBalanceDto addBalanceDto = getValidDtoBuilder().amount(BigDecimal.ZERO).build();
    Set<ConstraintViolation<AddBalanceDto>> violations = validator.validate(addBalanceDto);
    assertEquals(1, violations.size());
    assertEquals("{DecimalMin.invalid}", violations.iterator().next().getMessageTemplate());
  }
}
