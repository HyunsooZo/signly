package com.signly.template.application.preset;

import java.util.Map;

public record PresetSection(
        String sectionId,
        String type,
        int order,
        String content,
        Map<String, Object> metadata
) {
}
