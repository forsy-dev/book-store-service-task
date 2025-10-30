package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestDTO {

    @NotBlank(message = "{NotBlank.Order.clientEmail}")
    @Email(message = "{Email.Order.clientEmail}")
    private String clientEmail;

    @NotBlank(message = "{NotBlank.Order.employeeEmail}")
    @Email(message = "{Email.Order.employeeEmail}")
    private String employeeEmail;

    @NotNull(message = "{NotNull.Order.orderDate}")
    @PastOrPresent(message = "{PastOrPresent.Order.orderDate}")
    private LocalDateTime orderDate;

    @NotEmpty(message = "{NotEmpty.Order.bookItems}")
    private List<BookItemDTO> bookItems;
}

