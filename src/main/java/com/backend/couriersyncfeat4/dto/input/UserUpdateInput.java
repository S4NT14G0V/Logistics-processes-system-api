package com.backend.couriersyncfeat4.dto.input;

import jakarta.validation.constraints.Email;

public record UserUpdateInput(
        String name,
        @Email String email,
        Integer roleId
) {
}
