package com.signly.template.application.preset;

import java.util.List;

public record TemplatePreset(
        String id,
        String name,
        String description,
        List<PresetSection> sections
) {
}
