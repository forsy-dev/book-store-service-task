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

public class CreateOrderRequestDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private CreateOrderRequestDTO.CreateOrderRequestDTOBuilder getValidDtoBuilder() {
        return CreateOrderRequestDTO.builder()
                .clientEmail("client@client.com")
                .employeeEmail("employee@employee.com")
                .orderDate(LocalDateTime.now().minusDays(1))
                .bookItems(List.of(BookItemDTO.builder().build()));
    }

    @Test
    void whenAllFieldsAreValid_thenValidationSucceeds() {
        CreateOrderRequestDTO createOrderRequestDTO = getValidDtoBuilder().build();
        Set<ConstraintViolation<CreateOrderRequestDTO>> violations = validator.validate(createOrderRequestDTO);
        assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
    }

    @Nested
    class ClientEmailValidation {

        @Test
        void whenClientEmailIsNull_thenValidationFails() {
            CreateOrderRequestDTO createOrderRequestDTO = getValidDtoBuilder().clientEmail(null).build();
            Set<ConstraintViolation<CreateOrderRequestDTO>> violations = validator.validate(createOrderRequestDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.Order.clientEmail}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenClientEmailIsInvalid_thenValidationFails() {
            CreateOrderRequestDTO createOrderRequestDTO = getValidDtoBuilder().clientEmail("aa").build();
            Set<ConstraintViolation<CreateOrderRequestDTO>> violations = validator.validate(createOrderRequestDTO);
            assertEquals(1, violations.size());
            assertEquals("{Email.Order.clientEmail}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class EmployeeEmailValidation {

        @Test
        void whenEmployeeEmailIsNull_thenValidationFails() {
            CreateOrderRequestDTO createOrderRequestDTO = getValidDtoBuilder().employeeEmail(null).build();
            Set<ConstraintViolation<CreateOrderRequestDTO>> violations = validator.validate(createOrderRequestDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotBlank.Order.employeeEmail}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenEmployeeEmailIsInvalid_thenValidationFails() {
            CreateOrderRequestDTO createOrderRequestDTO = getValidDtoBuilder().employeeEmail("aa").build();
            Set<ConstraintViolation<CreateOrderRequestDTO>> violations = validator.validate(createOrderRequestDTO);
            assertEquals(1, violations.size());
            assertEquals("{Email.Order.employeeEmail}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class OrderDateValidation {

        @Test
        void whenOrderDateIsNull_thenValidationFails() {
            CreateOrderRequestDTO createOrderRequestDTO = getValidDtoBuilder().orderDate(null).build();
            Set<ConstraintViolation<CreateOrderRequestDTO>> violations = validator.validate(createOrderRequestDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotNull.Order.orderDate}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenOrderDateIsInFuture_thenValidationFails() {
            CreateOrderRequestDTO createOrderRequestDTO = getValidDtoBuilder().orderDate(LocalDateTime.now().plusDays(1)).build();
            Set<ConstraintViolation<CreateOrderRequestDTO>> violations = validator.validate(createOrderRequestDTO);
            assertEquals(1, violations.size());
            assertEquals("{PastOrPresent.Order.orderDate}", violations.iterator().next().getMessageTemplate());
        }
    }

    @Nested
    class BookItemsValidation {

        @Test
        void whenBookItemsIsNull_thenValidationFails() {
            CreateOrderRequestDTO createOrderRequestDTO = getValidDtoBuilder().bookItems(null).build();
            Set<ConstraintViolation<CreateOrderRequestDTO>> violations = validator.validate(createOrderRequestDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotEmpty.Order.bookItems}", violations.iterator().next().getMessageTemplate());
        }

        @Test
        void whenBookItemsIsEmpty_thenValidationFails() {
            CreateOrderRequestDTO createOrderRequestDTO = getValidDtoBuilder().bookItems(List.of()).build();
            Set<ConstraintViolation<CreateOrderRequestDTO>> violations = validator.validate(createOrderRequestDTO);
            assertEquals(1, violations.size());
            assertEquals("{NotEmpty.Order.bookItems}", violations.iterator().next().getMessageTemplate());
        }
    }
}
