package com.backend.couriersyncfeat4.dto.output;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        RoleResponse role
) {
}
