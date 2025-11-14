package com.signly.template.application.preset;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record TemplatePreset(
        String id,
        String name,
        String description,
        List<PresetSection> sections
) {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");

    public String renderHtml() {
        return sections.stream()
                .sorted((a, b) -> Integer.compare(a.order(), b.order()))
                .map(section -> convertVariablesToUnderlines(section.content()))
                .collect(Collectors.joining("\n"));
    }

    private String convertVariablesToUnderlines(String content) {
        if (content == null) {
            return "";
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuilder result = new StringBuilder();

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
