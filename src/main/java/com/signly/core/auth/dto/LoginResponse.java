package com.signly.core.auth.dto;

import com.signly.user.domain.model.UserType;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    String userId,
    String email,
    String name,
    UserType userType,
    long expiresIn
) {}