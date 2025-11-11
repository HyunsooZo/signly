package com.signly.template.application;

import com.signly.template.domain.model.TemplateContent;
import com.signly.template.domain.model.TemplateSection;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HtmlRenderer {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([a-zA-Z0-9_]+)}}");

    public String render(TemplateContent templateContent) {
        return render(templateContent, Map.of());
    }

    public String render(
            TemplateContent templateContent,
            Map<String, String> variableValues
    ) {
        List<TemplateSection> sections = templateContent.sections().stream()
                .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                .toList();

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"template-document\">\n");

        for (TemplateSection section : sections) {
            html.append(renderSection(section, variableValues));
            html.append("\n");
        }

        html.append("</div>");

        return html.toString();
    }

    private String renderSection(
            TemplateSection section,
            Map<String, String> variableValues
    ) {
        String content = substituteVariables(section.getContent(), variableValues);
        Map<String, Object> metadata = section.getMetadata();

        return switch (section.getType()) {
            case HEADER -> renderHeader(content, metadata);
            case PARAGRAPH -> renderParagraph(content, metadata);
            case DOTTED_BOX -> renderDottedBox(content, metadata);
            case FOOTER -> renderFooter(content, metadata);
            case CUSTOM -> renderCustom(content, metadata);
        };
    }

    private String renderHeader(
            String content,
            Map<String, Object> metadata
    ) {
        int level = metadata != null && metadata.containsKey("level") ?
                ((Number) metadata.get("level")).intValue() : 1;
        level = Math.max(1, Math.min(6, level));

        String alignment = metadata != null && metadata.containsKey("alignment") ?
                (String) metadata.get("alignment") : "center";

        String escaped = HtmlUtils.htmlEscape(content);

        String alignmentClass = getAlignmentClass(alignment);

        return String.format("<section class=\"template-heading%s\">" +
                        "<h%d>%s</h%d></section>",
                alignmentClass, level, escaped, level);
    }

    private String renderParagraph(
            String content,
            Map<String, Object> metadata
    ) {
        boolean indent = metadata != null && metadata.containsKey("indent") &&
                (Boolean) metadata.get("indent");

        String alignment = metadata != null && metadata.containsKey("alignment") ?
                (String) metadata.get("alignment") : "left";

        String escaped = HtmlUtils.htmlEscape(content).replace("\n", "<br>");

        String indentClass = indent ? " template-paragraph-indent" : "";
        String alignmentClass = getAlignmentClass(alignment);

        return String.format("<section class=\"template-paragraph%s%s\">" +
                        "<p>%s</p></section>",
                indentClass, alignmentClass, escaped);
    }

    private String renderDottedBox(
            String content,
            Map<String, Object> metadata
    ) {
        String alignment = metadata != null && metadata.containsKey("alignment") ?
                (String) metadata.get("alignment") : "left";

        String escaped = HtmlUtils.htmlEscape(content).replace("\n", "<br>");

        String alignmentClass = getAlignmentClass(alignment);

        return String.format("<section class=\"template-dotted-box%s\">" +
                        "<p>%s</p></section>",
                alignmentClass, escaped);
    }

    private String renderFooter(
            String content,
            Map<String, Object> metadata
    ) {
        String alignment = metadata != null && metadata.containsKey("alignment") ?
                (String) metadata.get("alignment") : "center";

        String escaped = HtmlUtils.htmlEscape(content).replace("\n", "<br>");

        String alignmentClass = getAlignmentClass(alignment);

        return String.format("<section class=\"template-footer%s\">" +
                        "<p>%s</p></section>",
                alignmentClass, escaped);
    }

    private String renderCustom(
            String content,
            Map<String, Object> metadata
    ) {
        boolean sanitize = metadata == null || !metadata.containsKey("sanitize") ||
                (Boolean) metadata.get("sanitize");

        if (sanitize) {
            return "<section class=\"template-custom\"><p>" +
                    HtmlUtils.htmlEscape(content) + "</p></section>";
        } else {
            return "<section class=\"template-custom\">" + content + "</section>";
        }
    }

    private String substituteVariables(
            String content,
            Map<String, String> variableValues
    ) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String value = variableValues.getOrDefault(variableName, "{{" + variableName + "}}");
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String getAlignmentClass(String alignment) {
        if (alignment == null || alignment.isEmpty()) {
            return "";
        }
        return switch (alignment.toLowerCase()) {
            case "center" -> " text-center";
            case "right" -> " text-right";
            case "justify" -> " text-justify";
            default -> "";  // left는 기본값이므로 클래스 불필요
        };
    }
}
