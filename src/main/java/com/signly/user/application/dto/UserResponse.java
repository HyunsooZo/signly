package com.signly.user.application.dto;

import com.signly.user.domain.model.UserStatus;
import com.signly.user.domain.model.UserType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public final class UserResponse {
    private String userId;
    private String email;
    private String name;
    private String companyName;
    private String businessPhone;
    private String businessAddress;
    private UserType userType;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}