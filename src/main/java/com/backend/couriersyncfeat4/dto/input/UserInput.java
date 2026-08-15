package com.backend.couriersyncfeat4.dto.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserInput(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotNull Integer roleId,
        @NotBlank @Size(min = 8) String temporaryPassword
) {
}
