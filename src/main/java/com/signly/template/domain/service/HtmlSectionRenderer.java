package com.signly.template.domain.service;

import com.signly.template.domain.model.TemplateSection;
import org.springframework.stereotype.Component;

/**
 * HTML 섹션 렌더러
 * SRP: 템플릿 섹션을 HTML로 렌더링 담당
 */
@Component
public class HtmlSectionRenderer {
    
    /**
     * 섹션을 HTML로 렌더링
     * @param section 템플릿 섹션
     * @param mode 렌더링 모드
     * @return 렌더링된 HTML
     */
    public String renderSection(TemplateSection section, RenderMode mode) {
        return switch (mode) {
            case PREVIEW -> renderForPreview(section);
            case CONTRACT -> renderForContract(section);
            case PDF -> renderForPdf(section);
        };
    }
    
    /**
     * 미리보기용 섹션 렌더링 (밑줄 포함)
     */
    private String renderForPreview(TemplateSection section) {
        String content = TemplateVariableUtils.convertVariablesToUnderlines(
            section.getContent(), true);
        return "<section class=\"template-section\" data-type=\"" + 
               section.getType() + "\"><p>" + content + "</p></section>";
    }
    
    /**
     * 계약서용 섹션 렌더링 (값 대입)
     */
    private String renderForContract(TemplateSection section) {
        String content = TemplateVariableUtils.escapeHtml(section.getContent());
        return "<section class=\"template-section\" data-type=\"" + 
               section.getType() + "\"><p>" + content + "</p></section>";
    }
    
    /**
     * PDF용 섹션 렌더링
     */
    private String renderForPdf(TemplateSection section) {
        String content = TemplateVariableUtils.escapeHtml(section.getContent());
        return "<section class=\"template-section\" data-type=\"" + 
               section.getType() + "\"><p>" + content + "</p></section>";
    }
}

/**
 * 렌더링 모드 enum
 */
enum RenderMode {
    /** 미리보기 모드 - 변수를 밑줄로 표시 */
    PREVIEW,
    /** 계약서 모드 - 변수를 그대로 표시 */
    CONTRACT,
    /** PDF 모드 - PDF 최적화 렌더링 */
    PDF
}