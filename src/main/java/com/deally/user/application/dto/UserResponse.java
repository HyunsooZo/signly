package com.deally.user.application.dto;

import com.deally.user.domain.model.UserStatus;
import com.deally.user.domain.model.UserType;

import java.time.LocalDateTime;

public record UserResponse(
        String userId,
        String email,
        String name,
        String companyName,
        String businessPhone,
        String businessAddress,
        UserType userType,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}