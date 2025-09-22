package com.signly.presentation.rest;

import com.signly.application.template.TemplateService;
import com.signly.application.template.dto.CreateTemplateCommand;
import com.signly.application.template.dto.TemplateResponse;
import com.signly.application.template.dto.UpdateTemplateCommand;
import com.signly.domain.template.model.TemplateStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Template", description = "계약서 템플릿 관리 API")
@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Operation(summary = "템플릿 생성", description = "새로운 계약서 템플릿을 생성합니다")
    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateTemplateCommand command) {
        TemplateResponse response = templateService.createTemplate(userId, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "템플릿 목록 조회", description = "사용자의 템플릿 목록을 조회합니다")
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

    @Operation(summary = "활성 템플릿 목록 조회", description = "사용자의 활성 상태 템플릿 목록을 조회합니다")
    @GetMapping("/active")
    public ResponseEntity<List<TemplateResponse>> getActiveTemplates(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-User-Id") String userId) {
        List<TemplateResponse> response = templateService.getActiveTemplates(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "템플릿 상세 조회", description = "특정 템플릿의 상세 정보를 조회합니다")
    @GetMapping("/{templateId}")
    public ResponseEntity<TemplateResponse> getTemplate(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "템플릿 ID", required = true) @PathVariable String templateId) {
        TemplateResponse response = templateService.getTemplate(userId, templateId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "템플릿 수정", description = "템플릿 내용을 수정합니다")
    @PutMapping("/{templateId}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "템플릿 ID", required = true) @PathVariable String templateId,
            @Valid @RequestBody UpdateTemplateCommand command) {
        TemplateResponse response = templateService.updateTemplate(userId, templateId, command);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "템플릿 활성화", description = "템플릿을 활성화 상태로 변경합니다")
    @PostMapping("/{templateId}/activate")
    public ResponseEntity<Void> activateTemplate(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "템플릿 ID", required = true) @PathVariable String templateId) {
        templateService.activateTemplate(userId, templateId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "템플릿 보관", description = "템플릿을 보관 상태로 변경합니다")
    @PostMapping("/{templateId}/archive")
    public ResponseEntity<Void> archiveTemplate(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "템플릿 ID", required = true) @PathVariable String templateId) {
        templateService.archiveTemplate(userId, templateId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "템플릿 삭제", description = "DRAFT 상태의 템플릿을 삭제합니다")
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "템플릿 ID", required = true) @PathVariable String templateId) {
        templateService.deleteTemplate(userId, templateId);
        return ResponseEntity.noContent().build();
    }
}