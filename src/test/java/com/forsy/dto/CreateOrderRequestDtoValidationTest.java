package com.forsy.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CreateOrderRequestDtoValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  private CreateOrderRequestDto.CreateOrderRequestDtoBuilder getValidDtoBuilder() {
    return CreateOrderRequestDto.builder()
        .clientEmail("client@client.com")
        .employeeEmail("employee@employee.com")
        .orderDate(LocalDateTime.now().minusDays(1))
        .bookItems(List.of(BookItemDto.builder().build()));
  }

  @Test
  void whenAllFieldsAreValid_thenValidationSucceeds() {
    CreateOrderRequestDto createOrderRequestDTO = getValidDtoBuilder().build();
    Set<ConstraintViolation<CreateOrderRequestDto>> violations = validator.validate(createOrderRequestDTO);
    assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
  }

  @Nested
  class ClientEmailValidation {

    @Test
    void whenClientEmailIsNull_thenValidationFails() {
      CreateOrderRequestDto createOrderRequestDTO = getValidDtoBuilder().clientEmail(null).build();
      Set<ConstraintViolation<CreateOrderRequestDto>> violations = validator.validate(createOrderRequestDTO);
      assertEquals(1, violations.size());
      assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenClientEmailIsInvalid_thenValidationFails() {
      CreateOrderRequestDto createOrderRequestDTO = getValidDtoBuilder().clientEmail("aa").build();
      Set<ConstraintViolation<CreateOrderRequestDto>> violations = validator.validate(createOrderRequestDTO);
      assertEquals(1, violations.size());
      assertEquals("{Email.invalid}", violations.iterator().next().getMessageTemplate());
    }
  }

  @Nested
  class EmployeeEmailValidation {

    @Test
    void whenEmployeeEmailIsNull_thenValidationFails() {
      CreateOrderRequestDto createOrderRequestDTO = getValidDtoBuilder().employeeEmail(null).build();
      Set<ConstraintViolation<CreateOrderRequestDto>> violations = validator.validate(createOrderRequestDTO);
      assertEquals(1, violations.size());
      assertEquals("{NotBlank.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenEmployeeEmailIsInvalid_thenValidationFails() {
      CreateOrderRequestDto createOrderRequestDTO = getValidDtoBuilder().employeeEmail("aa").build();
      Set<ConstraintViolation<CreateOrderRequestDto>> violations = validator.validate(createOrderRequestDTO);
      assertEquals(1, violations.size());
      assertEquals("{Email.invalid}", violations.iterator().next().getMessageTemplate());
    }
  }

  @Nested
  class OrderDateValidation {

    @Test
    void whenOrderDateIsNull_thenValidationFails() {
      CreateOrderRequestDto createOrderRequestDTO = getValidDtoBuilder().orderDate(null).build();
      Set<ConstraintViolation<CreateOrderRequestDto>> violations = validator.validate(createOrderRequestDTO);
      assertEquals(1, violations.size());
      assertEquals("{NotNull.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenOrderDateIsInFuture_thenValidationFails() {
      CreateOrderRequestDto createOrderRequestDTO = getValidDtoBuilder().orderDate(LocalDateTime.now().plusDays(1)).build();
      Set<ConstraintViolation<CreateOrderRequestDto>> violations = validator.validate(createOrderRequestDTO);
      assertEquals(1, violations.size());
      assertEquals("{PastOrPresent.invalid}", violations.iterator().next().getMessageTemplate());
    }
  }

  @Nested
  class BookItemsValidation {

    @Test
    void whenBookItemsIsNull_thenValidationFails() {
      CreateOrderRequestDto createOrderRequestDTO = getValidDtoBuilder().bookItems(null).build();
      Set<ConstraintViolation<CreateOrderRequestDto>> violations = validator.validate(createOrderRequestDTO);
      assertEquals(1, violations.size());
      assertEquals("{NotEmpty.Order.bookItems}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenBookItemsIsEmpty_thenValidationFails() {
      CreateOrderRequestDto createOrderRequestDTO = getValidDtoBuilder().bookItems(List.of()).build();
      Set<ConstraintViolation<CreateOrderRequestDto>> violations = validator.validate(createOrderRequestDTO);
      assertEquals(1, violations.size());
      assertEquals("{NotEmpty.Order.bookItems}", violations.iterator().next().getMessageTemplate());
    }
  }
}
