package com.signly.template.application.preset;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public final class PresetSection {
    private final String sectionId;
    private final String type;
    private final int order;
    private final String content;
    private final Map<String, Object> metadata;
}
