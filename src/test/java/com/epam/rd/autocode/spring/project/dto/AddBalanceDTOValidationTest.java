package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddBalanceDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private AddBalanceDTO.AddBalanceDTOBuilder getValidDtoBuilder() {
        return AddBalanceDTO.builder()
                .amount(new BigDecimal("19.99"));
    }

    @Test
    void whenAllFieldsAreValid_thenValidationSucceeds() {
        AddBalanceDTO addBalanceDTO = getValidDtoBuilder().build();
        Set<ConstraintViolation<AddBalanceDTO>> violations = validator.validate(addBalanceDTO);
        assertTrue(violations.isEmpty(), "A valid DTO should have no constraint violations");
    }

    @Test
    void whenBalanceIsNull_thenValidationFails() {
        AddBalanceDTO addBalanceDTO = getValidDtoBuilder().amount(null).build();
        Set<ConstraintViolation<AddBalanceDTO>> violations = validator.validate(addBalanceDTO);
        assertEquals(1, violations.size());
        assertEquals("{NotNull.invalid}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void whenBalanceIsNotTooSmall_thenValidationFails() {
        AddBalanceDTO addBalanceDTO = getValidDtoBuilder().amount(BigDecimal.ZERO).build();
        Set<ConstraintViolation<AddBalanceDTO>> violations = validator.validate(addBalanceDTO);
        assertEquals(1, violations.size());
        assertEquals("{DecimalMin.invalid}", violations.iterator().next().getMessageTemplate());
    }
}
