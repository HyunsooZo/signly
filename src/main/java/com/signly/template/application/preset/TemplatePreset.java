package com.signly.template.application.preset;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public final class TemplatePreset {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");
    private final String id;
    private final String name;
    private final String description;
    private final List<PresetSection> sections;

    public String renderHtml() {
        return sections.stream()
                .sorted((a, b) -> Integer.compare(a.getOrder(), b.getOrder()))
                .map(section -> convertVariablesToUnderlines(section.getContent()))
                .collect(Collectors.joining("\n"));
    }

    private String convertVariablesToUnderlines(String content) {
        if (content == null) {
            return "";
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String replacement = "<span class=\"blank-line\" data-variable-name=\"" +
                    escapeHtml(variableName) + "\"></span>";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
