package com.signly.contract.presentation.web;

import com.signly.contract.application.ContractService;
import com.signly.contract.application.dto.ContractResponse;
import com.signly.contract.application.dto.CreateContractCommand;
import com.signly.contract.application.dto.UpdateContractCommand;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.template.application.TemplateService;
import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.domain.model.TemplateStatus;
import com.signly.common.exception.BusinessException;
import com.signly.common.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/contracts")
public class ContractWebController {

    private static final Logger logger = LoggerFactory.getLogger(ContractWebController.class);
    private final ContractService contractService;
    private final TemplateService templateService;

    public ContractWebController(ContractService contractService, TemplateService templateService) {
        this.contractService = contractService;
        this.templateService = templateService;
    }

    @GetMapping
    public String contractList(@RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              @RequestParam(value = "status", required = false) ContractStatus status,
                              @RequestHeader(value = "X-User-Id", defaultValue = "demo-user") String userId,
                              Model model) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<ContractResponse> contracts = status != null ?
                    contractService.getContractsByOwnerAndStatus(userId, status, pageRequest) :
                    contractService.getContractsByOwner(userId, pageRequest);

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
                                 @RequestHeader(value = "X-User-Id", defaultValue = "demo-user") String userId,
                                 Model model) {
        try {
            ContractForm form = new ContractForm();

            // 템플릿이 지정된 경우 템플릿 정보 로드
            if (templateId != null && !templateId.isEmpty()) {
                TemplateResponse template = templateService.getTemplate(userId, templateId);
                form.setTemplateId(templateId);
                form.setTitle(template.title());
                form.setContent(template.content());
            }

            // 활성 템플릿 목록 로드
            PageRequest templatePageRequest = PageRequest.of(0, 100, Sort.by("title"));
            Page<TemplateResponse> activeTemplates = templateService.getTemplatesByOwnerAndStatus(
                    userId, TemplateStatus.ACTIVE, templatePageRequest);

            model.addAttribute("pageTitle", "새 계약서 생성");
            model.addAttribute("contract", form);
            model.addAttribute("templates", activeTemplates.getContent());
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
                                @RequestHeader(value = "X-User-Id", defaultValue = "demo-user") String userId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("pageTitle", "새 계약서 생성");
                return "contracts/form";
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

            ContractResponse response = contractService.createContract(userId, command);

            logger.info("계약서 생성 성공: {} (ID: {})", response.title(), response.id());
            redirectAttributes.addFlashAttribute("successMessage", "계약서가 성공적으로 생성되었습니다.");
            return "redirect:/contracts";

        } catch (ValidationException e) {
            logger.warn("계약서 생성 유효성 검사 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "새 계약서 생성");
            return "contracts/form";

        } catch (BusinessException e) {
            logger.warn("계약서 생성 비즈니스 로직 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "새 계약서 생성");
            return "contracts/form";

        } catch (Exception e) {
            logger.error("계약서 생성 중 예상치 못한 오류 발생", e);
            model.addAttribute("errorMessage", "계약서 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            model.addAttribute("pageTitle", "새 계약서 생성");
            return "contracts/form";
        }
    }

    @GetMapping("/{contractId}")
    public String contractDetail(@PathVariable String contractId,
                                @RequestHeader(value = "X-User-Id", defaultValue = "demo-user") String userId,
                                Model model) {
        try {
            ContractResponse contract = contractService.getContract(userId, contractId);

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
                                  @RequestHeader(value = "X-User-Id", defaultValue = "demo-user") String userId,
                                  Model model) {
        try {
            ContractResponse contract = contractService.getContract(userId, contractId);

            ContractForm form = new ContractForm();
            form.setTemplateId(contract.templateId());
            form.setTitle(contract.title());
            form.setContent(contract.content());
            form.setFirstPartyName(contract.firstParty().name());
            form.setFirstPartyEmail(contract.firstParty().email());
            form.setFirstPartyAddress(contract.firstParty().address());
            form.setSecondPartyName(contract.secondParty().name());
            form.setSecondPartyEmail(contract.secondParty().email());
            form.setSecondPartyAddress(contract.secondParty().address());
            form.setExpiresAt(contract.expiresAt());

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
                                @RequestHeader(value = "X-User-Id", defaultValue = "demo-user") String userId,
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

            ContractResponse response = contractService.updateContract(userId, contractId, command);

            logger.info("계약서 수정 성공: {} (ID: {})", response.title(), response.id());
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
                                @RequestHeader(value = "X-User-Id", defaultValue = "demo-user") String userId,
                                RedirectAttributes redirectAttributes) {
        try {
            contractService.sendForSigning(userId, contractId);
            logger.info("계약서 서명 요청 전송 성공: contractId={}", contractId);
            redirectAttributes.addFlashAttribute("successMessage", "계약서 서명 요청이 전송되었습니다.");
        } catch (Exception e) {
            logger.error("계약서 서명 요청 전송 중 오류 발생: contractId={}", contractId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "서명 요청 전송 중 오류가 발생했습니다.");
        }
        return "redirect:/contracts/" + contractId;
    }

    @PostMapping("/{contractId}/cancel")
    public String cancelContract(@PathVariable String contractId,
                                @RequestHeader(value = "X-User-Id", defaultValue = "demo-user") String userId,
                                RedirectAttributes redirectAttributes) {
        try {
            contractService.cancelContract(userId, contractId);
            logger.info("계약서 취소 성공: contractId={}", contractId);
            redirectAttributes.addFlashAttribute("successMessage", "계약서가 취소되었습니다.");
        } catch (Exception e) {
            logger.error("계약서 취소 중 오류 발생: contractId={}", contractId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "계약서 취소 중 오류가 발생했습니다.");
        }
        return "redirect:/contracts/" + contractId;
    }

    @PostMapping("/{contractId}/delete")
    public String deleteContract(@PathVariable String contractId,
                                @RequestHeader(value = "X-User-Id", defaultValue = "demo-user") String userId,
                                RedirectAttributes redirectAttributes) {
        try {
            contractService.deleteContract(userId, contractId);
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
        private LocalDateTime expiresAt;

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
    }
}