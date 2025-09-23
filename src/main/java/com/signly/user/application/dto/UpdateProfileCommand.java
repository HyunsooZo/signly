package com.signly.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileCommand(
        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다")
        String name,

        @Size(max = 200, message = "회사명은 200자를 초과할 수 없습니다")
        String companyName
) {
}