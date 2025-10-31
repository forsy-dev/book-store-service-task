package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddBalanceDTO {

    @NotNull(message = "{NotNull.invalid}")
    @DecimalMin(value = "0.01", message = "{DecimalMin.invalid}")
    private BigDecimal amount;
}
