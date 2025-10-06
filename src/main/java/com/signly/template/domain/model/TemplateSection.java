package com.signly.template.domain.model;

import com.signly.common.exception.ValidationException;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;

public class TemplateSection {

    private final String sectionId;
    private TemplateSectionType type;
    private int order;
    private String content;
    private final Map<String, Object> metadata;

    private TemplateSection(String sectionId,
                            TemplateSectionType type,
                            int order,
                            String content,
                            Map<String, Object> metadata) {
        this.sectionId = sectionId;
        this.type = type;
        this.order = order;
        this.content = content == null ? "" : content;
        this.metadata = metadata;
    }

    public static TemplateSection of(String sectionId,
                                     TemplateSectionType type,
                                     int order,
                                     String content,
                                     Map<String, Object> metadata) {
        if (sectionId == null || sectionId.trim().isEmpty()) {
            throw new ValidationException("섹션 ID는 필수입니다");
        }
        if (type == null) {
            throw new ValidationException("섹션 타입은 필수입니다");
        }
        return new TemplateSection(sectionId, type, order, content, metadata);
    }

    public String renderHtml() {
        String escaped = HtmlUtils.htmlEscape(this.content).replace("\n", "<br>");
        return switch (type) {
            case HEADER -> "<section class=\"template-header\"><h2 class=\"mb-0\">" + fallback(escaped, "머릿말을 입력하세요") + "</h2></section>";
            case DOTTED_BOX -> "<section class=\"template-dotted\"><div>" + fallback(escaped, "점선 박스 내용을 입력하세요") + "</div></section>";
            case FOOTER -> "<section class=\"template-footer\">" + fallback(escaped, "꼬릿말을 입력하세요") + "</section>";
            case CUSTOM -> allowsRawContent() ? content : "<section class=\"template-paragraph\"><p>" + fallback(escaped, "내용을 입력하세요") + "</p></section>";
            case PARAGRAPH -> defaultParagraph(escaped);
        };
    }

    private String defaultParagraph(String escaped) {
        // metadata에 kind가 'clause'면 조항 스타일로 렌더링 (번호는 CSS counter 사용)
        if (isClause()) {
            return "<section class=\"template-clause\"><p>" + fallback(escaped, "조항 내용을 입력하세요") + "</p></section>";
        }
        return "<section class=\"template-paragraph\"><p>" + fallback(escaped, "본문 내용을 입력하세요") + "</p></section>";
    }

    private boolean isClause() {
        return metadata != null && "clause".equals(metadata.get("kind"));
    }

    private boolean allowsRawContent() {
        return metadata != null && Boolean.TRUE.equals(metadata.get("rawHtml"));
    }

    private String fallback(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    public String toPlainText() {
        return (content == null ? "" : content).trim();
    }

    public String getSectionId() {
        return sectionId;
    }

    public TemplateSectionType getType() {
        return type;
    }

    public int getOrder() {
        return order;
    }

    public String getContent() {
        return content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setType(TemplateSectionType type) {
        this.type = type;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setContent(String content) {
        this.content = content == null ? "" : content;
    }
}
