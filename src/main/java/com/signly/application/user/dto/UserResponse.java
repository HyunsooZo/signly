package com.signly.application.user.dto;

import com.signly.domain.user.model.UserStatus;
import com.signly.domain.user.model.UserType;

import java.time.LocalDateTime;

public record UserResponse(
        String userId,
        String email,
        String name,
        String companyName,
        UserType userType,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}