package com.signly.template.domain.service;

import com.signly.template.domain.model.TemplateContent;
import com.signly.template.domain.model.TemplateMetadata;
import com.signly.template.domain.model.TemplateSection;
import com.signly.template.domain.model.TemplateSectionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnifiedTemplateRendererTest {

    @Mock
    private HtmlSectionRenderer sectionRenderer;

    @InjectMocks
    private UnifiedTemplateRenderer renderer;

    @Test
    @DisplayName("미리보기 모드 렌더링 테스트")
    void renderPreviewTest() {
        // Given
        TemplateMetadata templateMetadata = TemplateMetadata.of("테스트", "설명", "테스터", Map.of());
        TemplateSection section1 = TemplateSection.of("sec1", TemplateSectionType.HEADER, 1, "제목", null, null);
        TemplateSection section2 = TemplateSection.of("sec2", TemplateSectionType.PARAGRAPH, 2, "내용 [NAME]", null, null);
        TemplateContent content = TemplateContent.of(templateMetadata, List.of(section1, section2));

        when(sectionRenderer.renderSection(eq(section1), any(RenderMode.class)))
            .thenReturn("<section class=\"template-section\" data-type=\"HEADER\"><p>제목</p></section>");
        when(sectionRenderer.renderSection(eq(section2), any(RenderMode.class)))
            .thenReturn("<section class=\"template-section\" data-type=\"PARAGRAPH\"><p>내용 <span class=\"blank-line\" data-variable-name=\"NAME\"></span></p></section>");

        // When
        String result = renderer.renderPreview(content);

        // Then
        assertThat(result).contains("template-section")
                          .contains("data-type=\"HEADER\"")
                          .contains("data-type=\"PARAGRAPH\"")
                          .contains("blank-line");
    }

    @Test
    @DisplayName("계약서 모드 렌더링 테스트")
    void renderContractTest() {
        // Given
        TemplateMetadata templateMetadata = TemplateMetadata.of("테스트", "설명", "테스터", Map.of());
        TemplateSection section = TemplateSection.of("sec1", TemplateSectionType.PARAGRAPH, 1, "안녕하세요 [NAME]님", null, null);
        TemplateContent content = TemplateContent.of(templateMetadata, List.of(section));

        when(sectionRenderer.renderSection(eq(section), eq(RenderMode.CONTRACT)))
            .thenReturn("<section class=\"template-section\" data-type=\"PARAGRAPH\"><p>안녕하세요 [NAME]님</p></section>");

        // When
        String result = renderer.renderContract(content);

        // Then
        assertThat(result).contains("[NAME]")
                          .doesNotContain("blank-line");
    }

    @Test
    @DisplayName("PDF 모드 렌더링 테스트")
    void renderPdfTest() {
        // Given
        TemplateMetadata templateMetadata = TemplateMetadata.of("테스트", "설명", "테스터", Map.of());
        TemplateSection section = TemplateSection.of("sec1", TemplateSectionType.HEADER, 1, "계약서 제목", null, null);
        TemplateContent content = TemplateContent.of(templateMetadata, List.of(section));

        when(sectionRenderer.renderSection(eq(section), eq(RenderMode.PDF)))
            .thenReturn("<section class=\"template-section\" data-type=\"HEADER\"><p>계약서 제목</p></section>");

        // When
        String result = renderer.renderPdf(content);

        // Then
        assertThat(result).contains("계약서 제목")
                          .contains("data-type=\"HEADER\"");
    }

    @Test
    @DisplayName("변수 값 대입 렌더링 테스트")
    void renderWithVariablesTest() {
        // Given
        TemplateMetadata templateMetadata = TemplateMetadata.of("테스트", "설명", "테스터", Map.of());
        TemplateSection section = TemplateSection.of("sec1", TemplateSectionType.PARAGRAPH, 1, "안녕하세요 [NAME]님", null, null);
        TemplateContent content = TemplateContent.of(templateMetadata, List.of(section));
        Map<String, String> variables = Map.of("NAME", "홍길동");

        // When
        String result = renderer.renderWithVariables(content, variables);

        // Then
        assertThat(result).contains("홍길동")
                          .doesNotContain("[NAME]")
                          .contains("template-document");
    }

    @Test
    @DisplayName("일반 텍스트 렌더링 테스트")
    void renderPlainTextTest() {
        // Given
        TemplateMetadata templateMetadata = TemplateMetadata.of("테스트", "설명", "테스터", Map.of());
        TemplateSection section1 = TemplateSection.of("sec1", TemplateSectionType.HEADER, 1, "제목", null, null);
        TemplateSection section2 = TemplateSection.of("sec2", TemplateSectionType.PARAGRAPH, 2, "내용", null, null);
        TemplateContent content = TemplateContent.of(templateMetadata, List.of(section1, section2));

        // When
        String result = renderer.renderPlainText(content);

        // Then
        assertThat(result).isEqualTo("제목 • 내용");
    }

    @Test
    @DisplayName("고급 섹션 렌더링 - 헤더 레벨 테스트")
    void renderHeaderWithLevelTest() {
        // Given
        TemplateMetadata templateMetadata = TemplateMetadata.of("테스트", "설명", "테스터", Map.of());
        Map<String, Object> sectionMetadata = Map.of("level", 2, "alignment", "center");
        TemplateSection section = TemplateSection.of("sec1", TemplateSectionType.HEADER, 1, "부제목", sectionMetadata, null);
        TemplateContent content = TemplateContent.of(templateMetadata, List.of(section));

        // When
        String result = renderer.renderWithVariables(content, Map.of());

        // Then
        assertThat(result).contains("<h2>부제목</h2>")
                          .contains("template-heading")
                          .contains("text-center");
    }

    @Test
    @DisplayName("고급 섹션 렌더링 - 단락 들여쓰기 테스트")
    void renderParagraphWithIndentTest() {
        // Given
        TemplateMetadata templateMetadata = TemplateMetadata.of("테스트", "설명", "테스터", Map.of());
        Map<String, Object> sectionMetadata = Map.of("indent", true, "alignment", "right");
        TemplateSection section = TemplateSection.of("sec1", TemplateSectionType.PARAGRAPH, 1, "들여쓰기 내용", sectionMetadata, null);
        TemplateContent content = TemplateContent.of(templateMetadata, List.of(section));

        // When
        String result = renderer.renderWithVariables(content, Map.of());

        // Then
        assertThat(result).contains("template-paragraph-indent")
                          .contains("text-right")
                          .contains("들여쓰기 내용");
    }

    @Test
    @DisplayName("고급 섹션 렌더링 - 커스텀 섹션 테스트")
    void renderCustomSectionTest() {
        // Given
        TemplateMetadata templateMetadata = TemplateMetadata.of("테스트", "설명", "테스터", Map.of());
        Map<String, Object> sectionMetadata = Map.of("sanitize", false);
        TemplateSection section = TemplateSection.of("sec1", TemplateSectionType.CUSTOM, 1, "<b>볼드</b> 텍스트", sectionMetadata, null);
        TemplateContent content = TemplateContent.of(templateMetadata, List.of(section));

        // When
        String result = renderer.renderWithVariables(content, Map.of());

        // Then
        assertThat(result).contains("<b>볼드</b> 텍스트")
                          .doesNotContain("&lt;b&gt;")
                          .contains("template-custom");
    }

    @Test
    @DisplayName("HTML 이스케이프 테스트")
    void htmlEscapeTest() {
        // Given
        TemplateMetadata templateMetadata = TemplateMetadata.of("테스트", "설명", "테스터", Map.of());
        TemplateSection section = TemplateSection.of("sec1", TemplateSectionType.PARAGRAPH, 1, "<script>alert('xss')</script>", null, null);
        TemplateContent content = TemplateContent.of(templateMetadata, List.of(section));

        // When
        String result = renderer.renderWithVariables(content, Map.of());

        // Then
        assertThat(result).contains("&lt;script&gt;")
                          .doesNotContain("<script>");
    }
}