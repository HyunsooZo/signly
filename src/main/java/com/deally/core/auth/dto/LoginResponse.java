package com.deally.core.auth.dto;

import com.deally.user.domain.model.UserType;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String userId,
        String email,
        String name,
        String companyName,
        String businessPhone,
        String businessAddress,
        UserType userType,
        long expiresIn
) {}