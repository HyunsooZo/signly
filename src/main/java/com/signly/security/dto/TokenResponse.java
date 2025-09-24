package com.signly.security.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken
) {
}