package com.signly.template.application.preset;

import java.util.List;
import java.util.stream.Collectors;

public record TemplatePreset(
        String id,
        String name,
        String description,
        List<PresetSection> sections
) {
    public String renderHtml() {
        return sections.stream()
                .sorted((a, b) -> Integer.compare(a.order(), b.order()))
                .map(PresetSection::content)
                .collect(Collectors.joining("\n"));
    }
}
