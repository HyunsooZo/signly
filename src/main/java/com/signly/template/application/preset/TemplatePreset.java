package com.signly.template.application.preset;

import com.signly.template.domain.service.TemplateVariableUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 템플릿 프리셋 클래스
 * Refactored to use unified utilities and eliminate duplicate code.
 */
@Getter
@AllArgsConstructor
public final class TemplatePreset {
    private final String id;
    private final String name;
    private final String description;
    private final List<PresetSection> sections;

    /**
     * 프리셋을 HTML로 렌더링 (미리보기용)
     */
    public String renderHtml() {
        return sections.stream()
                .sorted((a, b) -> Integer.compare(a.getOrder(), b.getOrder()))
                .map(section -> TemplateVariableUtils.convertVariablesToUnderlines(section.getContent()))
                .collect(Collectors.joining("\n"));
    }
}
