package com.signly.template.presentation.rest;

import com.signly.template.application.TemplateService;
import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.application.preset.TemplatePresetService;
import com.signly.template.domain.model.TemplateStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 템플릿 조회 전용 REST API
 * 템플릿 생성/수정/삭제는 TemplateWebController에서 처리
 */
@RestController
@RequestMapping("/api/templates")
public class TemplateRestController {

    private final TemplateService templateService;
    private final TemplatePresetService presetService;

    public TemplateRestController(TemplateService templateService, TemplatePresetService presetService) {
        this.templateService = templateService;
        this.presetService = presetService;
    }

    @Operation(summary = "템플릿 목록 조회", description = "사용자의 템플릿 목록을 조회합니다 (계약서 생성 시 템플릿 선택용)")
    @GetMapping
    public ResponseEntity<Page<TemplateResponse>> getTemplates(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "템플릿 상태") @RequestParam(required = false) TemplateStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<TemplateResponse> response = status != null ?
                templateService.getTemplatesByOwnerAndStatus(userId, status, pageable) :
                templateService.getTemplatesByOwner(userId, pageable);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프리셋 템플릿 목록 조회", description = "사용 가능한 프리셋 템플릿 목록을 조회합니다")
    @GetMapping("/presets")
    public ResponseEntity<List<Map<String, Object>>> getPresetTemplates() {
        List<Map<String, Object>> presets = presetService.getSummaries().stream()
                .map(summary -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("presetId", summary.getId());
                    response.put("title", summary.getName());
                    response.put("description", summary.getDescription());
                    return response;
                })
                .toList();
        return ResponseEntity.ok(presets);
    }

    @Operation(summary = "프리셋 템플릿 상세 조회", description = "프리셋 템플릿의 상세 정보를 조회합니다")
    @GetMapping("/preset/{presetId}")
    public ResponseEntity<Map<String, Object>> getPresetTemplate(@PathVariable String presetId) {
        return presetService.getPreset(presetId)
                .map(preset -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("presetId", preset.getId());
                    response.put("title", preset.getName());
                    response.put("description", preset.getDescription());
                    response.put("renderedHtml", preset.renderHtml());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}