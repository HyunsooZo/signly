package com.signly.application.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTemplateCommand(
        @NotBlank(message = "템플릿 제목은 필수입니다")
        @Size(max = 255, message = "템플릿 제목은 255자를 초과할 수 없습니다")
        String title,

        @NotBlank(message = "템플릿 내용은 필수입니다")
        String content
) {
}