package com.deally.template.domain.service;

import com.deally.template.domain.model.TemplateContent;
import com.deally.template.domain.model.TemplateSection;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 통합 템플릿 렌더러
 * SRP: 모든 템플릿 렌더링 요구사항을 단일 진실 공급원으로 처리
 */
@Service
public class UnifiedTemplateRenderer {

    private final HtmlSectionRenderer sectionRenderer;

    public UnifiedTemplateRenderer(HtmlSectionRenderer sectionRenderer) {
        this.sectionRenderer = sectionRenderer;
    }

    /**
     * 템플릿 콘텐츠를 미리보기 HTML로 렌더링
     */
    public String renderPreview(TemplateContent templateContent) {
        return renderSections(templateContent.sections(), RenderMode.PREVIEW);
    }

    /**
     * 템플릿 콘텐츠를 계약서 HTML로 렌더링
     */
    public String renderContract(TemplateContent templateContent) {
        return renderSections(templateContent.sections(), RenderMode.CONTRACT);
    }

    /**
     * 템플릿 콘텐츠를 PDF용 HTML로 렌더링
     */
    public String renderPdf(TemplateContent templateContent) {
        return renderSections(templateContent.sections(), RenderMode.PDF);
    }

    /**
     * 템플릿 콘텐츠를 변수 값 대입하여 렌더링
     */
    public String renderWithVariables(
            TemplateContent templateContent,
            Map<String, String> variableValues
    ) {
        List<TemplateSection> sections = templateContent.sections().stream()
                .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                .toList();

        var html = new StringBuilder();
        html.append("<div class=\"template-document\">\n");

        for (var section : sections) {
            html.append(renderSectionWithVariables(section, variableValues));
            html.append("\n");
        }

        html.append("</div>");
        return html.toString();
    }

    /**
     * 템플릿 콘텐츠를 일반 텍스트로 렌더링
     */
    public String renderPlainText(TemplateContent templateContent) {
        return templateContent.sections().stream()
                .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                .map(TemplateSection::getContent)
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.joining(" • "));
    }

    /**
     * 섹션 목록을 지정된 모드로 렌더링
     */
    private String renderSections(
            List<TemplateSection> sections,
            RenderMode mode
    ) {
        return sections.stream()
                .sorted(Comparator.comparingInt(TemplateSection::getOrder))
                .map(section -> sectionRenderer.renderSection(section, mode))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 개별 섹션을 변수 값 대입하여 렌더링
     */
    private String renderSectionWithVariables(
            TemplateSection section,
            Map<String, String> variableValues
    ) {
        String content = TemplateVariableUtils.substituteVariables(section.getContent(), variableValues);
        return renderAdvancedSection(section.getType(), content, section.getMetadata());
    }

    /**
     * 고급 섹션 렌더링 (메타데이터 기반)
     */
    private String renderAdvancedSection(
            com.deally.template.domain.model.TemplateSectionType type,
            String content,
            Map<String, Object> metadata
    ) {
        return switch (type) {
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

        return String.format("<section class=\"template-heading%s\"><h%d>%s</h%d></section>",
                alignmentClass, level, escaped, level);
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

        return String.format("<section class=\"template-paragraph%s%s\"><p>%s</p></section>",
                indentClass, alignmentClass, escaped);
    }

    private String renderDottedBox(
            String content,
            Map<String, Object> metadata
    ) {
        String alignment = metadata != null && metadata.containsKey("alignment") ? (String) metadata.get("alignment") : "left";
        String escaped = HtmlUtils.htmlEscape(content).replace("\n", "<br>");
        String alignmentClass = getAlignmentClass(alignment);

        return String.format("<section class=\"template-dotted-box%s\"><p>%s</p></section>",
                alignmentClass, escaped);
    }

    private String renderFooter(
            String content,
            Map<String, Object> metadata
    ) {
        String alignment = metadata != null && metadata.containsKey("alignment") ? (String) metadata.get("alignment") : "center";
        String escaped = HtmlUtils.htmlEscape(content).replace("\n", "<br>");
        String alignmentClass = getAlignmentClass(alignment);

        return String.format("<section class=\"template-footer%s\"><p>%s</p></section>",
                alignmentClass, escaped);
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