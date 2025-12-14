package com.signly.template.presentation.rest;

import com.signly.template.application.TemplateService;
import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.application.preset.TemplatePresetService;
import com.signly.template.application.preset.TemplatePresetSummary;
import com.signly.template.application.preset.TemplatePreset;
import com.signly.template.domain.model.TemplateStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TemplateRestControllerTest {

    private TemplateRestController controller;
    private TemplateService templateService;
    private TemplatePresetService presetService;
    private HttpServletRequest request;
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        templateService = mock(TemplateService.class);
        presetService = mock(TemplatePresetService.class);
        request = mock(HttpServletRequest.class);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);

        controller = new TemplateRestController(templateService, presetService);

        // SecurityContext 모의 설정
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    @DisplayName("인증된 사용자는 템플릿 목록을 조회할 수 있다")
    void getTemplates_AuthenticatedUser_ReturnsTemplates() {
        // Given
        String userId = "user123";
        List<TemplateResponse> templates = List.of(
                new TemplateResponse("1", userId, "Template 1", "Content 1", 1, TemplateStatus.ACTIVE,
                        LocalDateTime.now(), LocalDateTime.now(), List.of(), "rendered", "preview", Map.of()),
                new TemplateResponse("2", userId, "Template 2", "Content 2", 1, TemplateStatus.ACTIVE,
                        LocalDateTime.now(), LocalDateTime.now(), List.of(), "rendered", "preview", Map.of())
        );
        Page<TemplateResponse> expectedPage = new PageImpl<>(templates);

        when(request.getAttribute("userId")).thenReturn(userId);
        when(templateService.getTemplatesByOwner(eq(userId), any(Pageable.class)))
                .thenReturn(expectedPage);

        // When
        ResponseEntity<Page<TemplateResponse>> response = controller.getTemplates(
                null, org.springframework.data.domain.PageRequest.of(0, 20), request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(expectedPage);
        verify(templateService).getTemplatesByOwner(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("인증은 되었지만 userId 속성이 없으면 400 Bad Request를 받는다")
    void getTemplates_AuthenticatedButNoUserId_ReturnsBadRequest() {
        // Given
        when(request.getAttribute("userId")).thenReturn(null);

        // When
        ResponseEntity<Page<TemplateResponse>> response = controller.getTemplates(
                null, null, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(templateService, never()).getTemplatesByOwner(any(), any());
    }

    @Test
    @DisplayName("상태 필터로 템플릿을 조회할 수 있다")
    void getTemplates_WithStatusFilter_ReturnsFilteredTemplates() {
        // Given
        String userId = "user123";
        TemplateStatus status = TemplateStatus.ACTIVE;
        List<TemplateResponse> templates = List.of(
                new TemplateResponse("1", userId, "Active Template", "Content", 1, status,
                        LocalDateTime.now(), LocalDateTime.now(), List.of(), "rendered", "preview", Map.of())
        );
        Page<TemplateResponse> expectedPage = new PageImpl<>(templates);

        when(request.getAttribute("userId")).thenReturn(userId);
        when(templateService.getTemplatesByOwnerAndStatus(eq(userId), eq(status), any(Pageable.class)))
                .thenReturn(expectedPage);

        // When
        ResponseEntity<Page<TemplateResponse>> response = controller.getTemplates(
                status, org.springframework.data.domain.PageRequest.of(0, 20), request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(expectedPage);
        verify(templateService).getTemplatesByOwnerAndStatus(eq(userId), eq(status), any(Pageable.class));
    }

    @Test
    @DisplayName("프리셋 템플릿 목록은 인증 없이 조회할 수 있다")
    void getPresetTemplates_NoAuthentication_ReturnsPresets() {
        // Given
        var summaries = List.of(
                new TemplatePresetSummary("preset1", "Standard Contract", "Standard employment contract"),
                new TemplatePresetSummary("preset2", "NDA", "Non-disclosure agreement")
        );

        when(presetService.getSummaries()).thenReturn(summaries);

        // When
        ResponseEntity<List<Map<String, Object>>> response = controller.getPresetTemplates();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        
        Map<String, Object> firstPreset = response.getBody().get(0);
        assertThat(firstPreset.get("presetId")).isEqualTo("preset1");
        assertThat(firstPreset.get("title")).isEqualTo("Standard Contract");
        assertThat(firstPreset.get("description")).isEqualTo("Standard employment contract");
    }

    @Test
    @DisplayName("존재하는 프리셋 템플릿은 상세 조회할 수 있다")
    void getPresetTemplate_ExistingPreset_ReturnsPresetDetails() {
        // Given
        String presetId = "preset1";
        var preset = mock(TemplatePreset.class);
        
        when(preset.getId()).thenReturn(presetId);
        when(preset.getName()).thenReturn("Standard Contract");
        when(preset.getDescription()).thenReturn("Standard employment contract");
        when(preset.renderHtml()).thenReturn("<html><body>Contract Content</body></html>");
        
        when(presetService.getPreset(presetId)).thenReturn(Optional.of(preset));

        // When
        ResponseEntity<Map<String, Object>> response = controller.getPresetTemplate(presetId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("presetId")).isEqualTo(presetId);
        assertThat(responseBody.get("title")).isEqualTo("Standard Contract");
        assertThat(responseBody.get("description")).isEqualTo("Standard employment contract");
        assertThat(responseBody.get("renderedHtml")).isEqualTo("<html><body>Contract Content</body></html>");
    }

    @Test
    @DisplayName("존재하지 않는 프리셋 템플릿은 404 Not Found를 반환한다")
    void getPresetTemplate_NonExistingPreset_ReturnsNotFound() {
        // Given
        String presetId = "nonexistent";
        when(presetService.getPreset(presetId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Map<String, Object>> response = controller.getPresetTemplate(presetId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}