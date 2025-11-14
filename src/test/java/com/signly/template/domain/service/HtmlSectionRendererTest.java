package com.signly.template.domain.service;

import com.signly.template.domain.model.TemplateSection;
import com.signly.template.domain.model.TemplateSectionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HtmlSectionRendererTest {

    @InjectMocks
    private HtmlSectionRenderer renderer;

    @Test
    @DisplayName("미리보기 모드 섹션 렌더링 테스트")
    void renderForPreviewTest() {
        // Given
        TemplateSection section = TemplateSection.of(
            "section1", 
            TemplateSectionType.PARAGRAPH, 
            1, 
            "안녕하세요 [NAME]님", 
            null,
            null
        );
        
        // When
        String result = renderer.renderSection(section, RenderMode.PREVIEW);
        
        // Then
        assertThat(result).contains("template-section")
                          .contains("data-type=\"PARAGRAPH\"")
                          .contains("blank-line")
                          .contains("data-variable-name=\"NAME\"");
    }

    @Test
    @DisplayName("계약서 모드 섹션 렌더링 테스트")
    void renderForContractTest() {
        // Given
        TemplateSection section = TemplateSection.of(
            "section2", 
            TemplateSectionType.PARAGRAPH, 
            1, 
            "안녕하세요 [NAME]님", 
            null,
            null
        );
        
        // When
        String result = renderer.renderSection(section, RenderMode.CONTRACT);
        
        // Then
        assertThat(result).contains("template-section")
                          .contains("data-type=\"PARAGRAPH\"")
                          .contains("[NAME]")
                          .doesNotContain("blank-line");
    }

    @Test
    @DisplayName("PDF 모드 섹션 렌더링 테스트")
    void renderForPdfTest() {
        // Given
        TemplateSection section = TemplateSection.of(
            "section3", 
            TemplateSectionType.HEADER, 
            1, 
            "계약서 제목", 
            null,
            null
        );
        
        // When
        String result = renderer.renderSection(section, RenderMode.PDF);
        
        // Then
        assertThat(result).contains("template-section")
                          .contains("data-type=\"HEADER\"")
                          .contains("계약서 제목");
    }

    @Test
    @DisplayName("HTML 이스케이프 테스트")
    void htmlEscapeTest() {
        // Given
        TemplateSection section = TemplateSection.of(
            "section4", 
            TemplateSectionType.PARAGRAPH, 
            1, 
            "<script>alert('xss')</script>", 
            null,
            null
        );
        
        // When
        String result = renderer.renderSection(section, RenderMode.CONTRACT);
        
        // Then
        assertThat(result).contains("&lt;script&gt;")
                          .doesNotContain("<script>");
    }

    @Test
    @DisplayName("서명 이미지 변수 미리보기 테스트")
    void signatureImagePreviewTest() {
        // Given
        TemplateSection section = TemplateSection.of(
            "section5", 
            TemplateSectionType.CUSTOM, 
            1, 
            "서명: [SIGNATURE_IMAGE]", 
            null,
            null
        );
        
        // When
        String result = renderer.renderSection(section, RenderMode.PREVIEW);
        
        // Then
        assertThat(result).contains("blank-line")
                          .contains("data-variable-name=\"SIGNATURE_IMAGE\"");
    }
}