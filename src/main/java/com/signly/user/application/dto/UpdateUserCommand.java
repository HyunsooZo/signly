package com.signly.user.application.dto;

public record UpdateUserCommand(
        String userId,
        String name,
        String companyName,
        String businessPhone,
        String businessAddress) {
}
