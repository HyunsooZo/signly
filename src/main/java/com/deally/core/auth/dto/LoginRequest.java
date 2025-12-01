package com.deally.core.auth.dto;

import com.deally.common.util.PasswordValidator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Pattern(regexp = PasswordValidator.PASSWORD_REGEX, message = PasswordValidator.PASSWORD_REQUIREMENT_MESSAGE)
        String password
) {}