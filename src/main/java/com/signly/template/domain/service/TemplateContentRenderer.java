package com.signly.template.domain.service;

import com.signly.template.domain.model.TemplateSection;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 템플릿 콘텐츠 렌더링 서비스
 * SRP: 템플릿 콘텐츠를 다양한 형식으로 렌더링 담당
 */
public class TemplateContentRenderer {

    /**
     * 섹션 목록을 HTML로 렌더링
     */
    public String renderToHtml(List<TemplateSection> sections) {
        return sections.stream()
                .sorted(java.util.Comparator.comparingInt(TemplateSection::getOrder))
                .map(this::renderSectionToHtml)
                .collect(Collectors.joining("\n"));
    }

    /**
     * 섹션 목록을 일반 텍스트로 렌더링
     */
    public String renderToPlainText(List<TemplateSection> sections) {
        return sections.stream()
                .sorted(java.util.Comparator.comparingInt(TemplateSection::getOrder))
                .map(TemplateSection::getContent)
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.joining(" • "));
    }

    /**
     * 개별 섹션을 HTML로 렌더링
     */
    private String renderSectionToHtml(TemplateSection section) {
        String content = section.getContent();
        return "<section class=\"template-section\" data-type=\"" + section.getType() + "\">" +
                "<p>" + content + "</p></section>";
    }
}