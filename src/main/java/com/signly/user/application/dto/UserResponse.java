package com.signly.user.application.dto;

import com.signly.user.domain.model.UserStatus;
import com.signly.user.domain.model.UserType;

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