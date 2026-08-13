package com.backend.couriersyncfeat4.dto.output;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
