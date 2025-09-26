package com.signly.template.presentation.web;

import com.signly.common.security.CurrentUserProvider;
import com.signly.common.security.SecurityUser;
import com.signly.template.application.TemplateService;
import com.signly.template.application.dto.CreateTemplateCommand;
import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.application.dto.UpdateTemplateCommand;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/templates")
public class TemplateWebController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateWebController.class);
    private final TemplateService templateService;
    private final CurrentUserProvider currentUserProvider;

    public TemplateWebController(TemplateService templateService, CurrentUserProvider currentUserProvider) {
        this.templateService = templateService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public String templateList(@RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              @RequestParam(value = "status", required = false) TemplateStatus status,
                              @RequestHeader(value = "X-User-Id", required = false) String userId,
                              @AuthenticationPrincipal SecurityUser securityUser,
                              HttpServletRequest request,
                              Model model) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, false);
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<TemplateResponse> templates = status != null ?
                    templateService.getTemplatesByOwnerAndStatus(resolvedUserId, status, pageRequest) :
                    templateService.getTemplatesByOwner(resolvedUserId, pageRequest);

            model.addAttribute("pageTitle", "템플릿 관리");
            model.addAttribute("templates", templates);
            model.addAttribute("currentStatus", status);
            model.addAttribute("statuses", TemplateStatus.values());

            return "templates/list";
        } catch (Exception e) {
            logger.error("템플릿 목록 조회 중 오류 발생", e);
            model.addAttribute("errorMessage", "템플릿 목록을 불러오는 중 오류가 발생했습니다.");
            return "templates/list";
        }
    }

    @GetMapping("/new")
    public String newTemplateForm(Model model) {
        model.addAttribute("pageTitle", "새 템플릿 생성");
        model.addAttribute("template", new TemplateForm());
        return "templates/form";
    }

    @PostMapping
    public String createTemplate(@Valid @ModelAttribute("template") TemplateForm form,
                                BindingResult bindingResult,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                HttpServletRequest request,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("pageTitle", "새 템플릿 생성");
                return "templates/form";
            }

            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            CreateTemplateCommand command = new CreateTemplateCommand(form.getTitle(), form.getContent());
            TemplateResponse response = templateService.createTemplate(resolvedUserId, command);

            logger.info("템플릿 생성 성공: {} (ID: {})", response.getTitle(), response.getTemplateId());
            redirectAttributes.addFlashAttribute("successMessage", "템플릿이 성공적으로 생성되었습니다.");
            return "redirect:/templates";

        } catch (ValidationException e) {
            logger.warn("템플릿 생성 유효성 검사 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "새 템플릿 생성");
            return "templates/form";

        } catch (BusinessException e) {
            logger.warn("템플릿 생성 비즈니스 로직 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "새 템플릿 생성");
            return "templates/form";

        } catch (Exception e) {
            logger.error("템플릿 생성 중 예상치 못한 오류 발생", e);
            model.addAttribute("errorMessage", "템플릿 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            model.addAttribute("pageTitle", "새 템플릿 생성");
            return "templates/form";
        }
    }

    @GetMapping("/{templateId}")
    public String templateDetail(@PathVariable String templateId,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                HttpServletRequest request,
                                Model model) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, false);
            TemplateResponse template = templateService.getTemplate(resolvedUserId, templateId);

            model.addAttribute("pageTitle", "템플릿 상세보기");
            model.addAttribute("template", template);
            return "templates/detail";

        } catch (Exception e) {
            logger.error("템플릿 상세 조회 중 오류 발생: templateId={}", templateId, e);
            model.addAttribute("errorMessage", "템플릿을 찾을 수 없습니다.");
            return "redirect:/templates";
        }
    }

    @GetMapping("/{templateId}/edit")
    public String editTemplateForm(@PathVariable String templateId,
                                  @RequestHeader(value = "X-User-Id", required = false) String userId,
                                  @AuthenticationPrincipal SecurityUser securityUser,
                                  HttpServletRequest request,
                                  Model model) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, false);
            TemplateResponse template = templateService.getTemplate(resolvedUserId, templateId);

            TemplateForm form = new TemplateForm();
            form.setTitle(template.getTitle());
            form.setContent(template.getContent());

            model.addAttribute("pageTitle", "템플릿 수정");
            model.addAttribute("template", form);
            model.addAttribute("templateId", templateId);
            return "templates/form";

        } catch (Exception e) {
            logger.error("템플릿 수정 폼 조회 중 오류 발생: templateId={}", templateId, e);
            model.addAttribute("errorMessage", "템플릿을 찾을 수 없습니다.");
            return "redirect:/templates";
        }
    }

    @PostMapping("/{templateId}")
    public String updateTemplate(@PathVariable String templateId,
                                @Valid @ModelAttribute("template") TemplateForm form,
                                BindingResult bindingResult,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                HttpServletRequest request,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("pageTitle", "템플릿 수정");
                model.addAttribute("templateId", templateId);
                return "templates/form";
            }

            UpdateTemplateCommand command = new UpdateTemplateCommand(form.getTitle(), form.getContent());
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            TemplateResponse response = templateService.updateTemplate(resolvedUserId, templateId, command);

            logger.info("템플릿 수정 성공: {} (ID: {})", response.getTitle(), response.getTemplateId());
            redirectAttributes.addFlashAttribute("successMessage", "템플릿이 성공적으로 수정되었습니다.");
            return "redirect:/templates/" + templateId;

        } catch (ValidationException e) {
            logger.warn("템플릿 수정 유효성 검사 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "템플릿 수정");
            model.addAttribute("templateId", templateId);
            return "templates/form";

        } catch (BusinessException e) {
            logger.warn("템플릿 수정 비즈니스 로직 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "템플릿 수정");
            model.addAttribute("templateId", templateId);
            return "templates/form";

        } catch (Exception e) {
            logger.error("템플릿 수정 중 예상치 못한 오류 발생", e);
            model.addAttribute("errorMessage", "템플릿 수정 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            model.addAttribute("pageTitle", "템플릿 수정");
            model.addAttribute("templateId", templateId);
            return "templates/form";
        }
    }

    @PostMapping("/{templateId}/activate")
    public String activateTemplate(@PathVariable String templateId,
                                  @RequestHeader(value = "X-User-Id", required = false) String userId,
                                  @AuthenticationPrincipal SecurityUser securityUser,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            templateService.activateTemplate(resolvedUserId, templateId);
            logger.info("템플릿 활성화 성공: templateId={}", templateId);
            redirectAttributes.addFlashAttribute("successMessage", "템플릿이 활성화되었습니다.");
        } catch (Exception e) {
            logger.error("템플릿 활성화 중 오류 발생: templateId={}", templateId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "템플릿 활성화 중 오류가 발생했습니다.");
        }
        return "redirect:/templates/" + templateId;
    }

    @PostMapping("/{templateId}/archive")
    public String archiveTemplate(@PathVariable String templateId,
                                 @RequestHeader(value = "X-User-Id", required = false) String userId,
                                 @AuthenticationPrincipal SecurityUser securityUser,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            templateService.archiveTemplate(resolvedUserId, templateId);
            logger.info("템플릿 보관 성공: templateId={}", templateId);
            redirectAttributes.addFlashAttribute("successMessage", "템플릿이 보관되었습니다.");
        } catch (Exception e) {
            logger.error("템플릿 보관 중 오류 발생: templateId={}", templateId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "템플릿 보관 중 오류가 발생했습니다.");
        }
        return "redirect:/templates/" + templateId;
    }

    @PostMapping("/{templateId}/delete")
    public String deleteTemplate(@PathVariable String templateId,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            templateService.deleteTemplate(resolvedUserId, templateId);
            logger.info("템플릿 삭제 성공: templateId={}", templateId);
            redirectAttributes.addFlashAttribute("successMessage", "템플릿이 삭제되었습니다.");
        } catch (Exception e) {
            logger.error("템플릿 삭제 중 오류 발생: templateId={}", templateId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "템플릿 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/templates";
    }

    public static class TemplateForm {
        private String title;
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
