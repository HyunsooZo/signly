package com.signly.contract.presentation.web;

import com.signly.common.exception.BusinessException;
import com.signly.common.exception.ValidationException;
import com.signly.common.security.CurrentUserProvider;
import com.signly.common.security.SecurityUser;
import com.signly.contract.application.ContractService;
import com.signly.contract.application.dto.ContractResponse;
import com.signly.contract.application.dto.CreateContractCommand;
import com.signly.contract.application.dto.UpdateContractCommand;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.template.application.TemplateService;
import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.application.preset.TemplatePresetService;
import com.signly.template.domain.model.TemplateStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/contracts")
public class ContractWebController {

    private static final Logger logger = LoggerFactory.getLogger(ContractWebController.class);
    private final ContractService contractService;
    private final TemplateService templateService;
    private final TemplatePresetService templatePresetService;
    private final CurrentUserProvider currentUserProvider;

    public ContractWebController(ContractService contractService,
                                TemplateService templateService,
                                TemplatePresetService templatePresetService,
                                CurrentUserProvider currentUserProvider) {
        this.contractService = contractService;
        this.templateService = templateService;
        this.templatePresetService = templatePresetService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public String contractList(@RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              @RequestParam(value = "status", required = false) ContractStatus status,
                              @RequestHeader(value = "X-User-Id", required = false) String userId,
                              @AuthenticationPrincipal SecurityUser securityUser,
                              HttpServletRequest request,
                              Model model) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<ContractResponse> contracts = contractService.getContractsByCreator(resolvedUserId, pageRequest);

            model.addAttribute("pageTitle", "계약서 관리");
            model.addAttribute("contracts", contracts);
            model.addAttribute("currentStatus", status);
            model.addAttribute("statuses", ContractStatus.values());

            return "contracts/list";
        } catch (Exception e) {
            logger.error("계약서 목록 조회 중 오류 발생", e);
            model.addAttribute("errorMessage", "계약서 목록을 불러오는 중 오류가 발생했습니다.");
            return "contracts/list";
        }
    }

    @GetMapping("/new")
    public String newContractForm(@RequestParam(value = "templateId", required = false) String templateId,
                                 @RequestParam(value = "preset", required = false) String preset,
                                 @RequestParam(value = "direct", required = false) Boolean direct,
                                 @RequestHeader(value = "X-User-Id", required = false) String userId,
                                 @AuthenticationPrincipal SecurityUser securityUser,
                                 HttpServletRequest request,
                                 Model model) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);

            // preset이나 direct 파라미터가 없으면 선택 화면으로
            if (preset == null && direct == null && templateId == null) {
                model.addAttribute("pageTitle", "계약서 유형 선택");
                model.addAttribute("presets", templatePresetService.getSummaries());
                return "contracts/select-type";
            }

            // 폼 화면으로 진행
            ContractForm form = new ContractForm();

            // 템플릿이 지정된 경우 템플릿 정보 로드
            if (templateId != null && !templateId.isEmpty()) {
                TemplateResponse template = templateService.getTemplate(resolvedUserId, templateId);
                form.setTemplateId(templateId);
                form.setTitle(template.getTitle());
                form.setContent(template.getContent());
            }

            // 활성 템플릿 목록 로드
            PageRequest templatePageRequest = PageRequest.of(0, 100, Sort.by("title"));
            Page<TemplateResponse> activeTemplates = templateService.getTemplatesByOwnerAndStatus(
                    resolvedUserId, TemplateStatus.ACTIVE, templatePageRequest);

            model.addAttribute("pageTitle", "새 계약서 생성");
            model.addAttribute("contract", form);
            model.addAttribute("templates", activeTemplates.getContent());
            model.addAttribute("presets", templatePresetService.getSummaries());
            model.addAttribute("selectedPreset", preset);
            return "contracts/form";

        } catch (Exception e) {
            logger.error("계약서 생성 폼 조회 중 오류 발생", e);
            model.addAttribute("errorMessage", "계약서 생성 페이지를 불러오는 중 오류가 발생했습니다.");
            return "redirect:/contracts";
        }
    }

    @PostMapping
    public String createContract(@Valid @ModelAttribute("contract") ContractForm form,
                                BindingResult bindingResult,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                HttpServletRequest request,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String resolvedUserId = null;
        try {
            resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);

            if (bindingResult.hasErrors()) {
                logger.warn("계약서 폼 검증 실패: {}", bindingResult.getAllErrors());
                return handleFormError("입력값을 확인해주세요.", model, form, resolvedUserId);
            }
            CreateContractCommand command = new CreateContractCommand(
                    form.getTemplateId(),
                    form.getTitle(),
                    form.getContent(),
                    form.getFirstPartyName(),
                    form.getFirstPartyEmail(),
                    form.getFirstPartyAddress(),
                    form.getSecondPartyName(),
                    form.getSecondPartyEmail(),
                    form.getSecondPartyAddress(),
                    form.getExpiresAt()
            );

            ContractResponse response = contractService.createContract(resolvedUserId, command);

            logger.info("계약서 생성 성공: {} (ID: {})", response.getTitle(), response.getId());
            redirectAttributes.addFlashAttribute("successMessage", "계약서가 성공적으로 생성되었습니다.");
            return "redirect:/contracts";

        } catch (ValidationException e) {
            logger.warn("계약서 생성 유효성 검사 실패: {}", e.getMessage());
            if (resolvedUserId == null) {
                resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, false);
            }
            return handleFormError(e.getMessage(), model, form, resolvedUserId);

        } catch (BusinessException e) {
            logger.warn("계약서 생성 비즈니스 로직 실패: {}", e.getMessage());
            if (resolvedUserId == null) {
                resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, false);
            }
            return handleFormError(e.getMessage(), model, form, resolvedUserId);

        } catch (Exception e) {
            logger.error("계약서 생성 중 예상치 못한 오류 발생", e);
            if (resolvedUserId == null) {
                resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, false);
            }
            return handleFormError("계약서 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", model, form, resolvedUserId);
        }
    }

    private String handleFormError(String errorMessage, Model model, ContractForm form, String userId) {
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("pageTitle", "새 계약서 생성");
        model.addAttribute("contract", form);
        model.addAttribute("presets", templatePresetService.getSummaries());

        // 템플릿 목록도 다시 로드
        try {
            PageRequest templatePageRequest = PageRequest.of(0, 100, Sort.by("title"));
            Page<TemplateResponse> activeTemplates = templateService.getTemplatesByOwnerAndStatus(
                    userId, TemplateStatus.ACTIVE, templatePageRequest);
            model.addAttribute("templates", activeTemplates.getContent());
        } catch (Exception e) {
            logger.warn("템플릿 목록 로드 실패", e);
            model.addAttribute("templates", java.util.Collections.emptyList());
        }

        return "contracts/form";
    }

    @GetMapping("/{contractId}")
    public String contractDetail(@PathVariable String contractId,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                HttpServletRequest request,
                                Model model) {
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
    public String editContractForm(@PathVariable String contractId,
                                  @RequestHeader(value = "X-User-Id", required = false) String userId,
                                  @AuthenticationPrincipal SecurityUser securityUser,
                                  HttpServletRequest request,
                                  Model model) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            ContractResponse contract = contractService.getContract(resolvedUserId, contractId);

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

            model.addAttribute("pageTitle", "계약서 수정");
            model.addAttribute("contract", form);
            model.addAttribute("contractId", contractId);
            return "contracts/form";

        } catch (Exception e) {
            logger.error("계약서 수정 폼 조회 중 오류 발생: contractId={}", contractId, e);
            model.addAttribute("errorMessage", "계약서를 찾을 수 없습니다.");
            return "redirect:/contracts";
        }
    }

    @PostMapping("/{contractId}")
    public String updateContract(@PathVariable String contractId,
                                @Valid @ModelAttribute("contract") ContractForm form,
                                BindingResult bindingResult,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                HttpServletRequest request,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("pageTitle", "계약서 수정");
                model.addAttribute("contractId", contractId);
                return "contracts/form";
            }

            UpdateContractCommand command = new UpdateContractCommand(
                    form.getTitle(),
                    form.getContent(),
                    form.getExpiresAt()
            );

            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            ContractResponse response = contractService.updateContract(resolvedUserId, contractId, command);

            logger.info("계약서 수정 성공: {} (ID: {})", response.getTitle(), response.getId());
            redirectAttributes.addFlashAttribute("successMessage", "계약서가 성공적으로 수정되었습니다.");
            return "redirect:/contracts/" + contractId;

        } catch (ValidationException e) {
            logger.warn("계약서 수정 유효성 검사 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "계약서 수정");
            model.addAttribute("contractId", contractId);
            return "contracts/form";

        } catch (BusinessException e) {
            logger.warn("계약서 수정 비즈니스 로직 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "계약서 수정");
            model.addAttribute("contractId", contractId);
            return "contracts/form";

        } catch (Exception e) {
            logger.error("계약서 수정 중 예상치 못한 오류 발생", e);
            model.addAttribute("errorMessage", "계약서 수정 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            model.addAttribute("pageTitle", "계약서 수정");
            model.addAttribute("contractId", contractId);
            return "contracts/form";
        }
    }

    @PostMapping("/{contractId}/send")
    public String sendForSigning(@PathVariable String contractId,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            contractService.sendForSigning(resolvedUserId, contractId);
            logger.info("계약서 서명 요청 전송 성공: contractId={}", contractId);
            redirectAttributes.addFlashAttribute("successMessage", "계약서 서명 요청이 전송되었습니다.");
        } catch (Exception e) {
            logger.error("계약서 서명 요청 전송 중 오류 발생: contractId={}", contractId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "서명 요청 전송 중 오류가 발생했습니다.");
        }
        return "redirect:/contracts/" + contractId;
    }

    @PostMapping("/{contractId}/resend")
    public String resendSigningEmail(@PathVariable String contractId,
                                    @RequestHeader(value = "X-User-Id", required = false) String userId,
                                    @AuthenticationPrincipal SecurityUser securityUser,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            contractService.resendSigningEmail(resolvedUserId, contractId);
            logger.info("계약서 서명 요청 재전송 성공: contractId={}", contractId);
            redirectAttributes.addFlashAttribute("successMessage", "서명 요청 이메일을 재전송했습니다.");
        } catch (Exception e) {
            logger.error("계약서 서명 요청 재전송 중 오류 발생: contractId={}", contractId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "서명 요청 재전송 중 오류가 발생했습니다.");
        }
        return "redirect:/contracts/" + contractId;
    }

    @PostMapping("/{contractId}/cancel")
    public String cancelContract(@PathVariable String contractId,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            contractService.cancelContract(resolvedUserId, contractId);
            logger.info("계약서 취소 성공: contractId={}", contractId);
            redirectAttributes.addFlashAttribute("successMessage", "계약서가 취소되었습니다.");
        } catch (Exception e) {
            logger.error("계약서 취소 중 오류 발생: contractId={}", contractId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "계약서 취소 중 오류가 발생했습니다.");
        }
        return "redirect:/contracts/" + contractId;
    }

    @PostMapping("/{contractId}/complete")
    public String completeContract(@PathVariable String contractId,
                                  @RequestHeader(value = "X-User-Id", defaultValue = "dbd51de0-b234-47d8-893b-241c744e7337") String userId,
                                  RedirectAttributes redirectAttributes) {
        try {
            contractService.completeContract(userId, contractId);
            logger.info("계약서 완료 처리 성공: contractId={}", contractId);
            redirectAttributes.addFlashAttribute("successMessage", "계약서가 완료되었습니다.");
        } catch (Exception e) {
            logger.error("계약서 완료 처리 중 오류 발생: contractId={}", contractId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "계약서 완료 처리 중 오류가 발생했습니다.");
        }
        return "redirect:/contracts/" + contractId;
    }

    @PostMapping("/{contractId}/delete")
    public String deleteContract(@PathVariable String contractId,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            contractService.deleteContract(resolvedUserId, contractId);
            logger.info("계약서 삭제 성공: contractId={}", contractId);
            redirectAttributes.addFlashAttribute("successMessage", "계약서가 삭제되었습니다.");
        } catch (Exception e) {
            logger.error("계약서 삭제 중 오류 발생: contractId={}", contractId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "계약서 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/contracts";
    }

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

        private static final DateTimeFormatter EXPIRES_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        public String getTemplateId() { return templateId; }
        public void setTemplateId(String templateId) { this.templateId = templateId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getFirstPartyName() { return firstPartyName; }
        public void setFirstPartyName(String firstPartyName) { this.firstPartyName = firstPartyName; }
        public String getFirstPartyEmail() { return firstPartyEmail; }
        public void setFirstPartyEmail(String firstPartyEmail) { this.firstPartyEmail = firstPartyEmail; }
        public String getFirstPartyAddress() { return firstPartyAddress; }
        public void setFirstPartyAddress(String firstPartyAddress) { this.firstPartyAddress = firstPartyAddress; }
        public String getSecondPartyName() { return secondPartyName; }
        public void setSecondPartyName(String secondPartyName) { this.secondPartyName = secondPartyName; }
        public String getSecondPartyEmail() { return secondPartyEmail; }
        public void setSecondPartyEmail(String secondPartyEmail) { this.secondPartyEmail = secondPartyEmail; }
        public String getSecondPartyAddress() { return secondPartyAddress; }
        public void setSecondPartyAddress(String secondPartyAddress) { this.secondPartyAddress = secondPartyAddress; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

        public String getExpiresAtInputValue() {
            return expiresAt != null ? expiresAt.format(EXPIRES_AT_FORMATTER) : "";
        }
    }
}
