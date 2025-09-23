package com.signly.application.auth.dto;

import com.signly.domain.user.model.UserType;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    String userId,
    String email,
    String name,
    UserType userType,
    long expiresIn
) {}