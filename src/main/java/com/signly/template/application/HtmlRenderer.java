package com.signly.template.application;

import com.signly.template.domain.model.TemplateContent;
import com.signly.template.domain.model.TemplateSection;
import com.signly.template.domain.model.TemplateSectionType;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class HtmlRenderer {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([a-zA-Z0-9_]+)}}");

    public String render(TemplateContent templateContent) {
        return render(templateContent, Map.of());
    }

    public String render(TemplateContent templateContent, Map<String, String> variableValues) {
        List<TemplateSection> sections = templateContent.getSections().stream()
                .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                .collect(Collectors.toList());

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"template-document\">\n");

        for (TemplateSection section : sections) {
            html.append(renderSection(section, variableValues));
            html.append("\n");
        }

        html.append("</div>");

        return html.toString();
    }

    private String renderSection(TemplateSection section, Map<String, String> variableValues) {
        String content = substituteVariables(section.getContent(), variableValues);
        Map<String, Object> metadata = section.getMetadata();

        return switch (section.getType()) {
            case HEADING -> renderHeading(content, metadata);
            case PARAGRAPH -> renderParagraph(content, metadata);
            case TABLE -> renderTable(metadata);
            case IMAGE -> renderImage(content, metadata);
            case DIVIDER -> renderDivider(metadata);
            case CUSTOM -> renderCustom(content, metadata);
        };
    }

    private String renderHeading(String content, Map<String, Object> metadata) {
        int level = metadata != null && metadata.containsKey("level") ?
                ((Number) metadata.get("level")).intValue() : 1;
        level = Math.max(1, Math.min(6, level));

        String alignment = metadata != null && metadata.containsKey("alignment") ?
                (String) metadata.get("alignment") : "left";

        String escaped = HtmlUtils.htmlEscape(content);

        return String.format("<section class=\"template-heading\" style=\"text-align: %s\">" +
                        "<h%d>%s</h%d></section>",
                alignment, level, escaped, level);
    }

    private String renderParagraph(String content, Map<String, Object> metadata) {
        boolean indent = metadata != null && metadata.containsKey("indent") &&
                (Boolean) metadata.get("indent");

        String alignment = metadata != null && metadata.containsKey("alignment") ?
                (String) metadata.get("alignment") : "left";

        String escaped = HtmlUtils.htmlEscape(content).replace("\n", "<br>");

        String indentClass = indent ? " template-paragraph-indent" : "";

        return String.format("<section class=\"template-paragraph%s\" style=\"text-align: %s\">" +
                        "<p>%s</p></section>",
                indentClass, alignment, escaped);
    }

    private String renderTable(Map<String, Object> metadata) {
        if (metadata == null || !metadata.containsKey("headers") || !metadata.containsKey("rows")) {
            return "<section class=\"template-table\"><p>테이블 데이터가 없습니다</p></section>";
        }

        @SuppressWarnings("unchecked")
        List<String> headers = (List<String>) metadata.get("headers");
        @SuppressWarnings("unchecked")
        List<List<String>> rows = (List<List<String>>) metadata.get("rows");

        StringBuilder table = new StringBuilder();
        table.append("<section class=\"template-table\">");
        table.append("<table class=\"table table-bordered\">");

        table.append("<thead><tr>");
        for (String header : headers) {
            table.append("<th>").append(HtmlUtils.htmlEscape(header)).append("</th>");
        }
        table.append("</tr></thead>");

        table.append("<tbody>");
        for (List<String> row : rows) {
            table.append("<tr>");
            for (String cell : row) {
                table.append("<td>").append(HtmlUtils.htmlEscape(cell)).append("</td>");
            }
            table.append("</tr>");
        }
        table.append("</tbody>");

        table.append("</table>");
        table.append("</section>");

        return table.toString();
    }

    private String renderImage(String content, Map<String, Object> metadata) {
        String width = metadata != null && metadata.containsKey("width") ?
                (String) metadata.get("width") : "auto";
        String height = metadata != null && metadata.containsKey("height") ?
                (String) metadata.get("height") : "auto";
        String alignment = metadata != null && metadata.containsKey("alignment") ?
                (String) metadata.get("alignment") : "center";
        String alt = metadata != null && metadata.containsKey("alt") ?
                (String) metadata.get("alt") : "이미지";

        return String.format("<section class=\"template-image\" style=\"text-align: %s\">" +
                        "<img src=\"%s\" alt=\"%s\" style=\"width: %s; height: %s;\"></section>",
                alignment, HtmlUtils.htmlEscape(content), HtmlUtils.htmlEscape(alt), width, height);
    }

    private String renderDivider(Map<String, Object> metadata) {
        String style = metadata != null && metadata.containsKey("style") ?
                (String) metadata.get("style") : "solid";

        return String.format("<section class=\"template-divider\">" +
                "<hr style=\"border-style: %s;\"></section>", style);
    }

    private String renderCustom(String content, Map<String, Object> metadata) {
        boolean sanitize = metadata == null || !metadata.containsKey("sanitize") ||
                (Boolean) metadata.get("sanitize");

        if (sanitize) {
            return "<section class=\"template-custom\"><p>" +
                    HtmlUtils.htmlEscape(content) + "</p></section>";
        } else {
            return "<section class=\"template-custom\">" + content + "</section>";
        }
    }

    private String substituteVariables(String content, Map<String, String> variableValues) {
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
}
