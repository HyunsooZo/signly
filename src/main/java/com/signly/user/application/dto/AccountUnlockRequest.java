package com.signly.user.application.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AccountUnlockRequest {

    @NotBlank(message = "잠금 해제 토큰은 필수입니다.")
    private String token;

    public AccountUnlockRequest(String token) {
        this.token = token;
    }

    public AccountUnlockRequest() {
    }
}