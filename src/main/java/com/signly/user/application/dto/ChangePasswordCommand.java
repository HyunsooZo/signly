package com.signly.user.application.dto;

import jakarta.validation.constraints.NotEmpty;

public record ChangePasswordCommand(
    @NotEmpty(message = "기존 비밀번호는 필수입니다")
    String oldPassword,

    @NotEmpty(message = "새 비밀번호는 필수입니다")
    String newPassword
) {}