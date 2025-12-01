package com.deally.template.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTemplateCommand(
        @NotBlank(message = "템플릿 제목은 필수입니다")
        @Size(max = 255, message = "템플릿 제목은 255자를 초과할 수 없습니다")
        String title,

        @NotBlank(message = "템플릿 섹션 정보는 필수입니다")
        String sectionsJson
) {
}
