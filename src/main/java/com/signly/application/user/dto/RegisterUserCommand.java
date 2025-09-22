package com.signly.application.user.dto;

import com.signly.domain.user.model.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUserCommand(
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이어야 합니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        String password,

        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다")
        String name,

        @Size(max = 200, message = "회사명은 200자를 초과할 수 없습니다")
        String companyName,

        @NotNull(message = "사용자 타입은 필수입니다")
        UserType userType
) {
}