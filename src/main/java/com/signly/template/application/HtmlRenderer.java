package com.signly.template.application;

import com.signly.template.domain.model.TemplateContent;
import com.signly.template.domain.model.TemplateSection;
import com.signly.template.domain.service.TemplateVariableUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Comparator;
import java.util.Map;

/**
 * HTML 렌더러
 * Refactored to use unified utilities and standardized variable format [VARIABLE_NAME].
 */
@Service
public class HtmlRenderer {

    public String render(TemplateContent templateContent) {
        return render(templateContent, Map.of());
    }

    public String render(
            TemplateContent templateContent,
            Map<String, String> variableValues
    ) {
        var sections = templateContent.sections().stream()
                .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                .toList();

        var html = new StringBuilder();
        html.append("<div class=\"template-document\">\n");

        for (var section : sections) {
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
        var content = TemplateVariableUtils.substituteVariables(section.getContent(), variableValues);
        var metadata = section.getMetadata();

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
        int level = metadata != null && metadata.containsKey("level") ? ((Number) metadata.get("level")).intValue() : 1;
        level = Math.max(1, Math.min(6, level));

        String alignment = metadata != null && metadata.containsKey("alignment") ? (String) metadata.get("alignment") : "center";
        String escaped = HtmlUtils.htmlEscape(content);
        String alignmentClass = getAlignmentClass(alignment);

        return String.format("<section class=\"template-heading%s\"><h%d>%s</h%d></section>", alignmentClass, level, escaped, level);
    }

    private String renderParagraph(
            String content,
            Map<String, Object> metadata
    ) {
        boolean indent = metadata != null && metadata.containsKey("indent") && (Boolean) metadata.get("indent");
        String alignment = metadata != null && metadata.containsKey("alignment") ? (String) metadata.get("alignment") : "left";
        String escaped = HtmlUtils.htmlEscape(content).replace("\n", "<br>");

        String indentClass = indent ? " template-paragraph-indent" : "";
        String alignmentClass = getAlignmentClass(alignment);

        return String.format("<section class=\"template-paragraph%s%s\"><p>%s</p></section>", indentClass, alignmentClass, escaped);
    }

    private String renderDottedBox(
            String content,
            Map<String, Object> metadata
    ) {
        String alignment = metadata != null && metadata.containsKey("alignment") ? (String) metadata.get("alignment") : "left";
        String escaped = HtmlUtils.htmlEscape(content).replace("\n", "<br>");
        String alignmentClass = getAlignmentClass(alignment);

        return String.format("<section class=\"template-dotted-box%s\"><p>%s</p></section>", alignmentClass, escaped);
    }

    private String renderFooter(
            String content,
            Map<String, Object> metadata
    ) {
        String alignment = metadata != null && metadata.containsKey("alignment") ? (String) metadata.get("alignment") : "center";
        String escaped = HtmlUtils.htmlEscape(content).replace("\n", "<br>");
        String alignmentClass = getAlignmentClass(alignment);

        return String.format("<section class=\"template-footer%s\"><p>%s</p></section>", alignmentClass, escaped);
    }

    private String renderCustom(
            String content,
            Map<String, Object> metadata
    ) {
        boolean sanitize = metadata == null || !metadata.containsKey("sanitize") || (Boolean) metadata.get("sanitize");

        if (sanitize) {
            return "<section class=\"template-custom\"><p>" + HtmlUtils.htmlEscape(content) + "</p></section>";
        } else {
            return "<section class=\"template-custom\">" + content + "</section>";
        }
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
