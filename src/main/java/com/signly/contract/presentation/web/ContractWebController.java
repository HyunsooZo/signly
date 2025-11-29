package com.signly.contract.presentation.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signly.common.exception.BusinessException;
import com.signly.common.exception.ValidationException;
import com.signly.common.security.CurrentUserProvider;
import com.signly.common.security.SecurityUser;
import com.signly.common.web.BaseWebController;
import com.signly.contract.application.ContractPdfService;
import com.signly.contract.application.ContractService;
import com.signly.contract.application.dto.ContractResponse;
import com.signly.contract.application.dto.CreateContractCommand;
import com.signly.contract.application.dto.UpdateContractCommand;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.contract.domain.model.GeneratedPdf;
import com.signly.contract.domain.model.PresetType;
import com.signly.signature.application.FirstPartySignatureService;
import com.signly.template.application.TemplateService;
import com.signly.template.application.VariableDefinitionService;
import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.application.dto.VariableDefinitionDto;
import com.signly.template.application.preset.TemplatePresetService;
import com.signly.template.domain.model.TemplateStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractWebController extends BaseWebController {

    private static final Logger logger = LoggerFactory.getLogger(ContractWebController.class);
    private final ContractService contractService;
    private final ContractPdfService contractPdfService;
    private final TemplateService templateService;
    private final TemplatePresetService templatePresetService;
    private final VariableDefinitionService variableDefinitionService;
    private final CurrentUserProvider currentUserProvider;
    private final FirstPartySignatureService firstPartySignatureService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final com.signly.common.storage.FileStorageService fileStorageService;

    @GetMapping
    public String contractList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) ContractStatus status,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model
    ) {
        return handleOperation(() -> {
            String resolvedUserId = resolveUserId(currentUserProvider, securityUser, request, userId, true);
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

            // 상태 필터링이 있으면 상태별로 조회, 없으면 전체 조회
            Page<ContractResponse> contracts;
            if (status != null) {
                contracts = contractService.getContractsByCreatorAndStatus(resolvedUserId, status, pageRequest);
            } else {
                contracts = contractService.getContractsByCreator(resolvedUserId, pageRequest);
            }

            addPageTitle(model, "계약서 관리");
            model.addAttribute("contracts", contracts);
            model.addAttribute("currentStatus", status);
            model.addAttribute("statuses", ContractStatus.values());

            return "contracts/list";
        }, "계약서 목록 조회", "contracts/list", model, "계약서 목록을 불러오는 중 오류가 발생했습니다.");
    }

    @GetMapping("/new")
    public String newContractForm(
            @RequestParam(value = "templateId", required = false) String templateId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            model.addAttribute("currentUserId", resolvedUserId);

            // 서명 존재 여부 체크
            if (!firstPartySignatureService.hasSignature(resolvedUserId)) {
                logger.warn("서명 없이 계약서 생성 시도: userId={}", resolvedUserId);
                redirectAttributes.addFlashAttribute("errorMessage",
                        "계약서를 생성하려면 먼저 서명을 등록해야 합니다.");
                redirectAttributes.addFlashAttribute("showSignatureAlert", true);
                return "redirect:/profile/signature";
            }

            // templateId가 없으면 유형 선택 화면으로 이동
            if (templateId == null) {
                model.addAttribute("pageTitle", "계약서 유형 선택");
                model.addAttribute("presets", templatePresetService.getSummaries());
                return "contracts/select-type";
            }

            // 폼 화면으로 진행
            ContractForm form = new ContractForm();
            form.setExpiresAt(LocalDateTime.now().plusHours(24));
            applyOwnerDefaults(securityUser, form, model);

            // 템플릿 정보 로드 (preset 또는 user template)
            String templateTitle;
            String templateContent;
            String renderedHtml;
            Object variables = new LinkedHashMap<>();

            // 먼저 preset인지 확인
            var presetOpt = templatePresetService.getPreset(templateId);
            if (presetOpt.isPresent()) {
                var preset = presetOpt.get();
                templateTitle = preset.getName();
                templateContent = ""; // preset은 content가 없음
                renderedHtml = preset.renderHtml();
                logger.info("[DEBUG] Loaded preset template: {}, renderedHtmlLength={}",
                        templateId, renderedHtml != null ? renderedHtml.length() : 0);
            } else {
                // preset이 아니면 user template 로드
                TemplateResponse template = templateService.getTemplate(resolvedUserId, templateId);
                templateTitle = template.getTitle();
                templateContent = template.getContent();
                renderedHtml = template.getRenderedHtml();
                variables = template.getVariables() != null ? template.getVariables() : new LinkedHashMap<>();
                logger.info("[DEBUG] Loaded user template: {}, renderedHtmlLength={}",
                        templateId, renderedHtml != null ? renderedHtml.length() : 0);
            }

            if (presetOpt.isPresent()) {
                form.setTemplateId(null);
                form.setSelectedPreset(templateId);
                model.addAttribute("selectedPreset", templateId);
            } else {
                form.setTemplateId(templateId);
            }
            form.setTitle(templateTitle);
            form.setContent(templateContent);

            model.addAttribute("selectedTemplate", templateId);
            try {
                Map<String, Object> templatePayload = new LinkedHashMap<>();
                templatePayload.put("templateId", templateId);
                templatePayload.put("title", templateTitle);
                templatePayload.put("renderedHtml", renderedHtml);
                templatePayload.put("variables", variables);
                String jsonPayload = objectMapper.writeValueAsString(templatePayload);
                logger.info("[DEBUG] Template JSON payload length: {}", jsonPayload.length());
                model.addAttribute("selectedTemplateContent", jsonPayload);
            } catch (Exception e) {
                logger.error("[ERROR] Failed to build template JSON", e);
            }

            // 활성 템플릿 목록 로드
            PageRequest templatePageRequest = PageRequest.of(0, 100, Sort.by("title"));
            Page<TemplateResponse> activeTemplates = templateService.getTemplatesByOwnerAndStatus(
                    resolvedUserId, TemplateStatus.ACTIVE, templatePageRequest);

            model.addAttribute("pageTitle", "새 계약서 생성");
            model.addAttribute("contract", form);
            model.addAttribute("templates", activeTemplates.getContent());
            model.addAttribute("presets", templatePresetService.getSummaries());
            addVariableDefinitionsToModel(model);
            return "contracts/form";

        } catch (Exception e) {
            logger.error("계약서 생성 폼 조회 중 오류 발생", e);
            model.addAttribute("errorMessage", "계약서 생성 페이지를 불러오는 중 오류가 발생했습니다.");
            return "redirect:/contracts";
        }
    }

    @PostMapping
    public String createContract(
            @Valid @ModelAttribute("contract") ContractForm form,
            BindingResult bindingResult,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        String resolvedUserId = resolveUserId(currentUserProvider, securityUser, request, userId, true);

        // 디버깅: 폼에서 받은 이메일 값 로깅
        logger.info("[DEBUG] 계약서 생성 폼 데이터 - firstPartyEmail: '{}', secondPartyEmail: '{}'",
                form.getFirstPartyEmail(), form.getSecondPartyEmail());

        if (bindingResult.hasErrors()) {
            logger.warn("계약서 폼 검증 실패: {}", bindingResult.getAllErrors());
            return handleFormError("입력값을 확인해주세요.", model, form, resolvedUserId);
        }

        try {
            PresetType presetType = PresetType.fromString(form.getSelectedPreset());
            CreateContractCommand command = new CreateContractCommand(
                    form.getTemplateId(),
                    form.getTitle(),
                    form.getContent(),
                    null,
                    form.getFirstPartyName(),
                    form.getFirstPartyEmail(),
                    form.getFirstPartyAddress(),
                    form.getSecondPartyName(),
                    form.getSecondPartyEmail(),
                    form.getSecondPartyAddress(),
                    form.getExpiresAt(),
                    presetType
            );

            ContractResponse response = contractService.createContract(resolvedUserId, command);
            logger.info("계약서 생성 성공: {} (ID: {})", response.getTitle(), response.getId());
            addSuccessMessage(redirectAttributes, "계약서가 성공적으로 생성되었습니다.");
            return "redirect:/contracts";
        } catch (com.signly.common.exception.ValidationException e) {
            logger.warn("계약서 생성 유효성 검사 실패: {}", e.getMessage());
            return handleFormError(e.getMessage(), model, form, resolvedUserId);
        } catch (Exception e) {
            logger.error("계약서 생성 중 오류 발생", e);
            return handleFormError("계약서 생성 중 오류가 발생했습니다. 다시 시도해주세요.", model, form, resolvedUserId);
        }
    }

    private String handleFormError(
            String errorMessage,
            Model model,
            ContractForm form,
            String userId
    ) {
        if (form.getExpiresAt() == null) {
            form.setExpiresAt(LocalDateTime.now().plusHours(24));
        }

        addErrorMessage(model, errorMessage);
        addPageTitle(model, "새 계약서 생성");
        model.addAttribute("contract", form);
        model.addAttribute("presets", templatePresetService.getSummaries());
        addVariableDefinitionsToModel(model);
        if (userId != null) {
            model.addAttribute("currentUserId", userId);
        }

        // 프리셋 정보 유지
        if (form.getSelectedPreset() != null) {
            model.addAttribute("selectedPreset", form.getSelectedPreset());
        }

        // 템플릿 목록도 다시 로드
        if (userId != null) {
            try {
                PageRequest templatePageRequest = PageRequest.of(0, 100, Sort.by("title"));
                Page<TemplateResponse> activeTemplates = templateService.getTemplatesByOwnerAndStatus(
                        userId, TemplateStatus.ACTIVE, templatePageRequest);
                model.addAttribute("templates", activeTemplates.getContent());
            } catch (Exception e) {
                logger.warn("템플릿 목록 로드 실패", e);
                model.addAttribute("templates", java.util.Collections.emptyList());
            }
        } else {
            model.addAttribute("templates", java.util.Collections.emptyList());
        }

        return "contracts/form";
    }

    @GetMapping("/{contractId}")
    public String contractDetail(
            @PathVariable String contractId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model
    ) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            ContractResponse contract = contractService.getContract(resolvedUserId, contractId);

            model.addAttribute("pageTitle", "계약서 상세보기");
            model.addAttribute("contract", contract);
            return "contracts/detail";

        } catch (Exception e) {
            logger.error("계약서 상세 조회 중 오류 발생: contractId={}", contractId, e);
            model.addAttribute("errorMessage", "계약서를 찾을 수 없습니다.");
            return "redirect:/contracts";
        }
    }

    @GetMapping("/{contractId}/edit")
    public String editContractForm(
            @PathVariable String contractId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model
    ) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            ContractResponse contract = contractService.getContract(resolvedUserId, contractId);

            model.addAttribute("currentUserId", resolvedUserId);

            ContractForm form = new ContractForm();
            form.setTemplateId(contract.getTemplateId());
            form.setTitle(contract.getTitle());
            form.setContent(contract.getContent());
            form.setFirstPartyName(contract.getFirstParty().getName());
            form.setFirstPartyEmail(contract.getFirstParty().getEmail());
            form.setFirstPartyAddress(contract.getFirstParty().getOrganizationName());
            form.setSecondPartyName(contract.getSecondParty().getName());
            form.setSecondPartyEmail(contract.getSecondParty().getEmail());
            form.setSecondPartyAddress(contract.getSecondParty().getOrganizationName());
            form.setExpiresAt(contract.getExpiresAtLocalDateTime());

            try {
                Map<String, Object> existingContractPayload = new LinkedHashMap<>();
                existingContractPayload.put("title", contract.getTitle());
                existingContractPayload.put("content", contract.getContent());
                existingContractPayload.put("secondPartyEmail", contract.getSecondParty().getEmail());
                existingContractPayload.put("secondPartyName", contract.getSecondParty().getName());
                existingContractPayload.put("firstPartyName", contract.getFirstParty().getName());
                existingContractPayload.put("firstPartyEmail", contract.getFirstParty().getEmail());
                existingContractPayload.put("firstPartyAddress", contract.getFirstParty().getOrganizationName());
                model.addAttribute("existingContractJson", objectMapper.writeValueAsString(existingContractPayload));
            } catch (Exception e) {
                logger.warn("[DEBUG] Edit form - Failed to build existing contract JSON: {}", e.getMessage());
            }

            // 프리셋 타입 확인 및 설정
            logger.info("[DEBUG] Edit form - contractId: {}, presetType: {}", contractId, contract.getPresetType());
            if (contract.getPresetType() != null && contract.getPresetType() != PresetType.NONE) {
                String presetValue = contract.getPresetType().toDisplayString();
                form.setSelectedPreset(presetValue);
                model.addAttribute("selectedPreset", presetValue);
                logger.info("[DEBUG] Edit form - selectedPreset set to: {}", presetValue);
            } else {
                logger.warn("[DEBUG] Edit form - No preset type found or preset is NONE");
            }

            // 템플릿 정보 조회 (수정 모드에서도 템플릿 HTML을 로드하여 작성 화면과 동일하게 표시)
            if (contract.getTemplateId() != null && !contract.getTemplateId().isEmpty()) {
                try {
                    var template = templateService.getTemplate(resolvedUserId, contract.getTemplateId());

                    // 템플릿을 selectedTemplate으로 설정하여 작성 화면과 동일하게 로드
                    model.addAttribute("selectedTemplate", template);

                    // 템플릿 내용을 JSON으로 전달
                    Map<String, Object> templatePayload = new LinkedHashMap<>();
                    templatePayload.put("templateId", template.getTemplateId());
                    templatePayload.put("title", template.getTitle());
                    templatePayload.put("renderedHtml", template.getRenderedHtml());
                    templatePayload.put("variables", template.getVariables());
                    model.addAttribute("selectedTemplateContent", objectMapper.writeValueAsString(templatePayload));

                    logger.info("[DEBUG] Edit form - template loaded as selectedTemplate: {}", template.getTitle());
                } catch (Exception e) {
                    logger.warn("[DEBUG] Edit form - Failed to load template: {}", e.getMessage());
                }
            }

            model.addAttribute("pageTitle", "계약서 수정");
            model.addAttribute("contract", form);
            model.addAttribute("contractId", contractId);
            model.addAttribute("templates", java.util.Collections.emptyList());
            model.addAttribute("presets", templatePresetService.getSummaries());
            addVariableDefinitionsToModel(model);
            return "contracts/form";

        } catch (Exception e) {
            logger.error("계약서 수정 폼 조회 중 오류 발생: contractId={}", contractId, e);
            model.addAttribute("errorMessage", "계약서를 찾을 수 없습니다.");
            return "redirect:/contracts";
        }
    }

    @PostMapping("/{contractId}")
    public String updateContract(
            @PathVariable String contractId,
            @Valid @ModelAttribute("contract") ContractForm form,
            BindingResult bindingResult,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);

            if (bindingResult.hasErrors()) {
                model.addAttribute("pageTitle", "계약서 수정");
                model.addAttribute("contractId", contractId);
                model.addAttribute("currentUserId", resolvedUserId);
                addVariableDefinitionsToModel(model);
                return "contracts/form";
            }

            UpdateContractCommand command = new UpdateContractCommand(
                    form.getTitle(),
                    form.getContent(),
                    form.getExpiresAt()
            );

            model.addAttribute("currentUserId", resolvedUserId);
            ContractResponse response = contractService.updateContract(resolvedUserId, contractId, command);

            logger.info("계약서 수정 성공: {} (ID: {})", response.getTitle(), response.getId());
            redirectAttributes.addFlashAttribute("successMessage", "계약서가 성공적으로 수정되었습니다.");
            return "redirect:/contracts/" + contractId;

        } catch (ValidationException e) {
            logger.warn("계약서 수정 유효성 검사 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "계약서 수정");
            model.addAttribute("contractId", contractId);
            addVariableDefinitionsToModel(model);
            if (securityUser != null) {
                String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
                model.addAttribute("currentUserId", resolvedUserId);
            }
            return "contracts/form";

        } catch (BusinessException e) {
            logger.warn("계약서 수정 비즈니스 로직 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "계약서 수정");
            model.addAttribute("contractId", contractId);
            addVariableDefinitionsToModel(model);
            if (securityUser != null) {
                String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
                model.addAttribute("currentUserId", resolvedUserId);
            }
            return "contracts/form";

        } catch (Exception e) {
            logger.error("계약서 수정 중 예상치 못한 오류 발생", e);
            model.addAttribute("errorMessage", "계약서 수정 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            model.addAttribute("pageTitle", "계약서 수정");
            model.addAttribute("contractId", contractId);
            addVariableDefinitionsToModel(model);
            if (securityUser != null) {
                String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
                model.addAttribute("currentUserId", resolvedUserId);
            }
            return "contracts/form";
        }
    }

    @PostMapping("/{contractId}/send")
    public String sendForSigning(
            @PathVariable String contractId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        return handleOperationWithRedirect(() -> {
            String resolvedUserId = resolveUserId(currentUserProvider, securityUser, request, userId, true);
            contractService.sendForSigning(resolvedUserId, contractId);
            logger.info("계약서 서명 요청 전송 성공: contractId={}", contractId);
            addSuccessMessage(redirectAttributes, "계약서 서명 요청이 전송되었습니다.");
        }, "계약서 서명 요청 전송", "/contracts/" + contractId, redirectAttributes, "서명 요청 전송 중 오류가 발생했습니다.");
    }

    @PostMapping("/{contractId}/resend")
    public String resendSigningEmail(
            @PathVariable String contractId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        return handleOperationWithRedirect(() -> {
            String resolvedUserId = resolveUserId(currentUserProvider, securityUser, request, userId, true);
            contractService.resendSigningEmail(resolvedUserId, contractId);
            logger.info("계약서 서명 요청 재전송 성공: contractId={}", contractId);
            addSuccessMessage(redirectAttributes, "서명 요청 이메일을 재전송했습니다.");
        }, "계약서 서명 요청 재전송", "/contracts/" + contractId, redirectAttributes, "서명 요청 재전송 중 오류가 발생했습니다.");
    }

    @PostMapping("/{contractId}/cancel")
    public String cancelContract(
            @PathVariable String contractId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        return handleOperationWithRedirect(() -> {
            String resolvedUserId = resolveUserId(currentUserProvider, securityUser, request, userId, true);
            contractService.cancelContract(resolvedUserId, contractId);
            logger.info("계약서 취소 성공: contractId={}", contractId);
            addSuccessMessage(redirectAttributes, "계약서가 취소되었습니다.");
        }, "계약서 취소", "/contracts/" + contractId, redirectAttributes, "계약서 취소 중 오류가 발생했습니다.");
    }

    @PostMapping("/{contractId}/delete")
    public String deleteContract(
            @PathVariable String contractId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        return handleOperationWithRedirect(() -> {
            String resolvedUserId = resolveUserId(currentUserProvider, securityUser, request, userId, true);
            contractService.deleteContract(resolvedUserId, contractId);
            logger.info("계약서 삭제 성공: contractId={}", contractId);
            addSuccessMessage(redirectAttributes, "계약서가 삭제되었습니다.");
        }, "계약서 삭제", "/contracts", redirectAttributes, "계약서 삭제 중 오류가 발생했습니다.");
    }

    @GetMapping("/{contractId}/pdf-view")
    public String viewPdf(
            @PathVariable String contractId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model
    ) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            ContractResponse contract = contractService.getContract(resolvedUserId, contractId);

            // SIGNED 상태가 아니면 접근 불가
            if (contract.getStatus() != ContractStatus.SIGNED) {
                logger.warn("서명 완료되지 않은 계약서 PDF 뷰어 접근 시도: contractId={}, status={}",
                        contractId, contract.getStatus());
                model.addAttribute("errorMessage", "서명이 완료된 계약서만 PDF로 볼 수 있습니다.");
                return "redirect:/contracts/" + contractId;
            }

            model.addAttribute("pageTitle", "계약서 PDF");
            model.addAttribute("contract", contract);
            return "contracts/pdf-view";

        } catch (Exception e) {
            logger.error("PDF 뷰어 화면 조회 중 오류 발생: contractId={}", contractId, e);
            model.addAttribute("errorMessage", "PDF 뷰어를 불러오는 중 오류가 발생했습니다.");
            return "redirect:/contracts";
        }
    }

    @GetMapping("/{contractId}/pdf/download")
    public void downloadPdf(
            @PathVariable String contractId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            ContractResponse contract = contractService.getContract(resolvedUserId, contractId);

            // SIGNED 상태가 아니면 다운로드 불가
            if (contract.getStatus() != ContractStatus.SIGNED) {
                logger.warn("서명 완료되지 않은 계약서 PDF 다운로드 시도: contractId={}, status={}",
                        contractId, contract.getStatus());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "서명이 완료된 계약서만 다운로드할 수 있습니다.");
                return;
            }

            GeneratedPdf pdf;

            // 저장된 PDF가 있으면 파일에서 읽기, 없으면 생성
            if (contract.getPdfPath() != null && !contract.getPdfPath().isEmpty()) {
                logger.info("저장된 PDF 파일 제공: contractId={}, path={}", contractId, contract.getPdfPath());
                try {
                    byte[] pdfContent = fileStorageService.loadFile(contract.getPdfPath());
                    String fileName = contract.getTitle() + "_" +
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
                    pdf = GeneratedPdf.of(pdfContent, fileName);
                } catch (Exception e) {
                    logger.warn("저장된 PDF 로드 실패, 새로 생성: contractId={}", contractId, e);
                    pdf = contractPdfService.generateContractPdf(contractId);
                }
            } else {
                logger.info("저장된 PDF 없음, 새로 생성: contractId={}", contractId);
                pdf = contractPdfService.generateContractPdf(contractId);
            }

            // 파일명 인코딩 (한글 파일명 지원)
            String encodedFileName = URLEncoder.encode(pdf.fileName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");

            // HTTP 응답 헤더 설정
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            response.setContentLengthLong(pdf.sizeInBytes());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

            // PDF 바이트 스트림으로 전송
            try (OutputStream out = response.getOutputStream()) {
                out.write(pdf.content());
                out.flush();
            }

            logger.info("PDF 다운로드 성공: contractId={}, fileName={}", contractId, pdf.fileName());

        } catch (Exception e) {
            logger.error("PDF 다운로드 중 오류 발생: contractId={}", contractId, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "PDF 다운로드 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/{contractId}/pdf/inline")
    public void viewPdfInline(
            @PathVariable String contractId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            ContractResponse contract = contractService.getContract(resolvedUserId, contractId);

            // SIGNED 상태가 아니면 조회 불가
            if (contract.getStatus() != ContractStatus.SIGNED) {
                logger.warn("서명 완료되지 않은 계약서 PDF 조회 시도: contractId={}, status={}",
                        contractId, contract.getStatus());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "서명이 완료된 계약서만 조회할 수 있습니다.");
                return;
            }

            GeneratedPdf pdf;

            // 저장된 PDF가 있으면 파일에서 읽기, 없으면 생성
            if (contract.getPdfPath() != null && !contract.getPdfPath().isEmpty()) {
                logger.info("저장된 PDF 파일 제공: contractId={}, path={}", contractId, contract.getPdfPath());
                try {
                    byte[] pdfContent = fileStorageService.loadFile(contract.getPdfPath());
                    String fileName = contract.getTitle() + "_" +
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
                    pdf = GeneratedPdf.of(pdfContent, fileName);
                } catch (Exception e) {
                    logger.warn("저장된 PDF 로드 실패, 새로 생성: contractId={}", contractId, e);
                    pdf = contractPdfService.generateContractPdf(contractId);
                }
            } else {
                logger.info("저장된 PDF 없음, 새로 생성: contractId={}", contractId);
                pdf = contractPdfService.generateContractPdf(contractId);
            }

            // 파일명 인코딩
            String encodedFileName = URLEncoder.encode(pdf.fileName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");

            // HTTP 응답 헤더 설정 (inline으로 표시)
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            response.setContentLengthLong(pdf.sizeInBytes());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

            // PDF 바이트 스트림으로 전송
            try (OutputStream out = response.getOutputStream()) {
                out.write(pdf.content());
                out.flush();
            }

            logger.info("PDF 인라인 뷰 성공: contractId={}, fileName={}", contractId, pdf.fileName());

        } catch (Exception e) {
            logger.error("PDF 인라인 뷰 중 오류 발생: contractId={}", contractId, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "PDF 조회 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/variables")
    @ResponseBody
    public ResponseEntity<java.util.List<VariableDefinitionDto>> getVariableDefinitions() {
        return ResponseEntity.ok(variableDefinitionService.getAllActiveVariables());
    }

    @GetMapping("/variables/grouped")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, java.util.List<VariableDefinitionDto>>> getVariableDefinitionsGrouped() {
        return ResponseEntity.ok(variableDefinitionService.getVariablesByCategory());
    }

    /**
     * 변수 정의를 JSON 문자열로 변환하여 Model에 추가
     */
    private void addVariableDefinitionsToModel(Model model) {
        try {
            String variableDefinitionsJson = objectMapper.writeValueAsString(
                variableDefinitionService.getAllActiveVariables()
            );
            model.addAttribute("variableDefinitionsJson", variableDefinitionsJson);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize variable definitions to JSON", e);
            // 실패 시 빈 배열로 폴백
            model.addAttribute("variableDefinitionsJson", "[]");
        }
    }

    private void applyOwnerDefaults(
            SecurityUser securityUser,
            ContractForm form,
            Model model
    ) {
        if (securityUser == null || form == null) {
            return;
        }

        if (form.getFirstPartyName() == null || form.getFirstPartyName().isBlank()) {
            form.setFirstPartyName(securityUser.getName());
        }
        if (form.getFirstPartyEmail() == null || form.getFirstPartyEmail().isBlank()) {
            form.setFirstPartyEmail(securityUser.getEmail());
        }
        if (form.getFirstPartyAddress() == null || form.getFirstPartyAddress().isBlank()) {
            String addressFallback = securityUser.getBusinessAddress();
            if (addressFallback == null || addressFallback.isBlank()) {
                addressFallback = securityUser.getCompanyName();
            }
            if (addressFallback != null) {
                form.setFirstPartyAddress(addressFallback);
            }
        }

        model.addAttribute("currentUserName", securityUser.getName());
        model.addAttribute("currentUserEmail", securityUser.getEmail());
        model.addAttribute("currentUserCompany", securityUser.getCompanyName());
        model.addAttribute("currentUserBusinessPhone", securityUser.getBusinessPhone());
        model.addAttribute("currentUserBusinessAddress", securityUser.getBusinessAddress());
    }

    @Setter
    @Getter
    public static class ContractForm {
        private String templateId;
        private String title;
        private String content;
        private String firstPartyName;
        private String firstPartyEmail;
        private String firstPartyAddress;
        private String secondPartyName;
        private String secondPartyEmail;
        private String secondPartyAddress;
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime expiresAt;
        private String selectedPreset;

        private static final DateTimeFormatter EXPIRES_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        public String getExpiresAtInputValue() {
            return expiresAt != null ? expiresAt.format(EXPIRES_AT_FORMATTER) : "";
        }

    }
}
