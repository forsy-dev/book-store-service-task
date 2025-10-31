package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordDTO {

    @NotBlank(message = "{NotBlank.invalid}")
    private String oldPassword;

    @NotBlank(message = "{NotBlank.invalid}")
    @Size(max = 100, message = "{Size.invalid}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "{Pattern.password}")
    private String newPassword;
}
