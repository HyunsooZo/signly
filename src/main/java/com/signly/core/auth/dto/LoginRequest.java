package com.signly.core.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$", 
             message = "비밀번호에는 한글을 포함할 수 없습니다. 영문, 숫자, 특수문자만 입력 가능합니다.")
    String password
) {}