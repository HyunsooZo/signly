package com.signly.template.presentation.rest;

import com.signly.template.application.TemplateService;
import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.domain.model.TemplateStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 템플릿 조회 전용 REST API
 * 템플릿 생성/수정/삭제는 TemplateWebController에서 처리
 */
@RestController
@RequestMapping("/api/templates")
public class TemplateRestController {

    private final TemplateService templateService;

    public TemplateRestController(TemplateService templateService) {
        this.templateService = templateService;
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
}