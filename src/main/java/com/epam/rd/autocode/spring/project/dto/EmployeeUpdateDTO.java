package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeUpdateDTO {

    @NotBlank(message = "{NotBlank.invalid}")
    @Size(min = 3, max = 255, message = "{Size.invalid}")
    private String name;

    @NotNull(message = "{NotNull.invalid}")
    @PastOrPresent(message = "{PastOrPresent.invalid}")
    private LocalDate birthDate;

    @NotBlank(message = "{NotBlank.invalid}")
    @Pattern(regexp = "^\\+?[0-9\\s\\(\\)-]{10,20}$", message = "{Pattern.phone}")
    private String phone;
}
