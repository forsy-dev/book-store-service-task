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

    @NotBlank(message = "{NotBlank.invalid}")
    @Email(message = "{Email.invalid}")
    private String clientEmail;

    @NotBlank(message = "{NotBlank.invalid}")
    @Email(message = "{Email.invalid}")
    private String employeeEmail;

    @NotNull(message = "{NotNull.invalid}")
    @PastOrPresent(message = "{PastOrPresent.invalid}")
    private LocalDateTime orderDate;

    @NotEmpty(message = "{NotEmpty.Order.bookItems}")
    private List<BookItemDTO> bookItems;
}

