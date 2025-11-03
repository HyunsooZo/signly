package com.signly.template.presentation.web;

import com.signly.common.exception.BusinessException;
import com.signly.common.exception.ValidationException;
import com.signly.common.security.CurrentUserProvider;
import com.signly.common.security.SecurityUser;
import com.signly.common.web.BaseWebController;
import com.signly.template.application.TemplateService;
import com.signly.template.application.dto.CreateTemplateCommand;
import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.application.dto.UpdateTemplateCommand;
import com.signly.template.application.preset.PresetSection;
import com.signly.template.application.preset.TemplatePresetService;
import com.signly.template.application.preset.TemplatePresetSummary;
import com.signly.template.domain.model.TemplateStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/templates")
public class TemplateWebController extends BaseWebController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateWebController.class);
    private final TemplateService templateService;
    private final CurrentUserProvider currentUserProvider;
    private final TemplatePresetService templatePresetService;

    public TemplateWebController(
            TemplateService templateService,
            CurrentUserProvider currentUserProvider,
            TemplatePresetService templatePresetService
    ) {
        this.templateService = templateService;
        this.currentUserProvider = currentUserProvider;
        this.templatePresetService = templatePresetService;
    }

    @GetMapping
    public String templateList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) TemplateStatus status,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model
    ) {
        return handleOperation(() -> {
            String resolvedUserId = resolveUserId(currentUserProvider, securityUser, request, userId, true);
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<TemplateResponse> templates = status != null ?
                    templateService.getTemplatesByOwnerAndStatus(resolvedUserId, status, pageRequest) :
                    templateService.getTemplatesByOwner(resolvedUserId, pageRequest);

            addPageTitle(model, "템플릿 관리");
            model.addAttribute("templates", templates);
            model.addAttribute("currentStatus", status);
            model.addAttribute("statuses", TemplateStatus.values());

            return "templates/list";
        }, "템플릿 목록 조회", "templates/list", model, "템플릿 목록을 불러오는 중 오류가 발생했습니다.");
    }

    @GetMapping("/new")
    public String newTemplateForm(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model
    ) {
        return handleOperation(() -> {
            String resolvedUserId = resolveUserId(currentUserProvider, securityUser, request, userId, true);
            addPageTitle(model, "새 템플릿 생성");
            model.addAttribute("template", new TemplateForm());
            model.addAttribute("presets", templatePresetService.getSummaries());
            model.addAttribute("currentUserId", resolvedUserId);
            return "templates/form";
        }, "템플릿 생성 폼 조회", "redirect:/templates", model, "템플릿 생성 폼을 불러오는 중 오류가 발생했습니다.");
    }

    @PostMapping
    public String createTemplate(
            @Valid @ModelAttribute("template") TemplateForm form,
            BindingResult bindingResult,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addPageTitle(model, "새 템플릿 생성");
            model.addAttribute("presets", templatePresetService.getSummaries());
            return "templates/form";
        }

        return handleOperation(() -> {
            String resolvedUserId = resolveUserId(currentUserProvider, securityUser, request, userId, true);
            CreateTemplateCommand command = new CreateTemplateCommand(form.getTitle(), form.getSectionsJson());
            TemplateResponse response = templateService.createTemplate(resolvedUserId, command);

            logger.info("템플릿 생성 성공: {} (ID: {})", response.getTitle(), response.getTemplateId());
            addSuccessMessage(redirectAttributes, "템플릿이 성공적으로 생성되었습니다.");
            return "redirect:/templates";
        }, "템플릿 생성", "templates/form", model, "템플릿 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    @GetMapping("/{templateId}")
    public String templateDetail(
            @PathVariable String templateId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model
    ) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
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
    public String editTemplateForm(
            @PathVariable String templateId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model
    ) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            TemplateResponse template = templateService.getTemplate(resolvedUserId, templateId);

            TemplateForm form = new TemplateForm();
            form.setTitle(template.getTitle());
            form.setSectionsJson(template.getSectionsJson());

            model.addAttribute("pageTitle", "템플릿 수정");
            model.addAttribute("template", form);
            model.addAttribute("templateId", templateId);
            model.addAttribute("presets", templatePresetService.getSummaries());
            return "templates/form";

        } catch (Exception e) {
            logger.error("템플릿 수정 폼 조회 중 오류 발생: templateId={}", templateId, e);
            model.addAttribute("errorMessage", "템플릿을 찾을 수 없습니다.");
            return "redirect:/templates";
        }
    }

    @PostMapping("/{templateId}")
    public String updateTemplate(
            @PathVariable String templateId,
            @Valid @ModelAttribute("template") TemplateForm form,
            BindingResult bindingResult,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("pageTitle", "템플릿 수정");
                model.addAttribute("templateId", templateId);
                model.addAttribute("presets", templatePresetService.getSummaries());
                return "templates/form";
            }

            UpdateTemplateCommand command = new UpdateTemplateCommand(form.getTitle(), form.getSectionsJson());
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
            model.addAttribute("presets", templatePresetService.getSummaries());
            return "templates/form";

        } catch (BusinessException e) {
            logger.warn("템플릿 수정 비즈니스 로직 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "템플릿 수정");
            model.addAttribute("templateId", templateId);
            model.addAttribute("presets", templatePresetService.getSummaries());
            return "templates/form";

        } catch (Exception e) {
            logger.error("템플릿 수정 중 예상치 못한 오류 발생", e);
            model.addAttribute("errorMessage", "템플릿 수정 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            model.addAttribute("pageTitle", "템플릿 수정");
            model.addAttribute("templateId", templateId);
            model.addAttribute("presets", templatePresetService.getSummaries());
            return "templates/form";
        }
    }

    @GetMapping("/presets/{presetId}")
    @ResponseBody
    public ResponseEntity<TemplatePresetResponse> getPreset(@PathVariable String presetId) {
        return templatePresetService.getPreset(presetId)
                .map(preset -> {
                    String renderedHtml = preset.sections().stream()
                            .map(section -> section.content() != null ? section.content() : "")
                            .collect(java.util.stream.Collectors.joining("\n"));
                    return ResponseEntity.ok(
                            new TemplatePresetResponse(preset.id(), preset.name(), preset.sections(), renderedHtml)
                    );
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping({"/presets", "/presets/"})
    @ResponseBody
    public ResponseEntity<java.util.List<TemplatePresetSummary>> getPresets() {
        return ResponseEntity.ok(templatePresetService.getSummaries());
    }

    @GetMapping("/presets/{presetId}/sections")
    @ResponseBody
    public ResponseEntity<TemplatePresetSectionsResponse> getPresetSections(@PathVariable String presetId) {
        return templatePresetService.getPreset(presetId)
                .map(preset -> {
                    java.util.List<PresetSection> sections;
                    
                    // 단일 섹션에 rawHtml이 있는 경우 파싱 필요
                    if (preset.sections().size() == 1) {
                        PresetSection singleSection = preset.sections().get(0);
                        java.util.Map<String, Object> metadata = singleSection.metadata();
                        
                        if (metadata != null && Boolean.TRUE.equals(metadata.get("rawHtml"))) {
                            // HTML을 여러 섹션으로 파싱
                            sections = parseHtmlToSections(singleSection.content());
                        } else {
                            // 일반적인 경우 그대로 사용
                            sections = preset.sections();
                        }
                    } else {
                        // 이미 여러 섹션으로 분리된 경우
                        sections = preset.sections();
                    }
                    
                    return ResponseEntity.ok(
                            new TemplatePresetSectionsResponse(preset.id(), preset.name(), sections)
                    );
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private java.util.List<PresetSection> parseHtmlToSections(String html) {
        java.util.List<PresetSection> sections = new java.util.ArrayList<>();
        
        try {
            // Jsoup 사용하여 HTML 파싱
            Document doc = Jsoup.parse(html);
            int order = 0;
            
            // title 클래스 → title 섹션
            Elements titleElements = doc.select(".title");
            for (Element element : titleElements) {
                sections.add(createSection("title", element.text(), order++, java.util.Map.of("kind", "title")));
            }
            
            // contract-intro 클래스 → text 섹션
            Elements introElements = doc.select(".contract-intro");
            for (Element element : introElements) {
                sections.add(createSection("text", element.text(), order++, java.util.Map.of("kind", "text")));
            }
            
            // section 클래스 → clause 섹션 (번호 추출)
            Elements sectionElements = doc.select(".section");
            for (Element element : sectionElements) {
                String content = element.text();
                String type = element.select(".section-number").isEmpty() ? "text" : "clause";
                sections.add(createSection(type, content, order++, java.util.Map.of("kind", type)));
            }
            
            // date-section 클래스 → text 섹션
            Elements dateElements = doc.select(".date-section");
            for (Element element : dateElements) {
                sections.add(createSection("text", element.text(), order++, java.util.Map.of("kind", "text")));
            }
            
            // signature-section 클래스 → signature 섹션
            Elements signatureElements = doc.select(".signature-section");
            for (Element element : signatureElements) {
                String content = element.html(); // HTML 구조 유지
                sections.add(createSection("signature", content, order++, 
                    java.util.Map.of("kind", "signature", "signature", true)));
            }
            
        } catch (Exception e) {
            logger.error("Failed to parse HTML to sections", e);
            // 실패 시 단일 섹션으로 fallback
            sections.add(createSection("text", html, 0, java.util.Map.of("kind", "text")));
        }
        
        return sections;
    }
    
    private PresetSection createSection(String type, String content, int order, java.util.Map<String, Object> metadata) {
        return new PresetSection(
                "preset-section-" + order,
                type,
                order,
                content,
                metadata
        );
    }

    private record TemplatePresetSectionsResponse(String id, String name, 
                                                  java.util.List<com.signly.template.application.preset.PresetSection> sections) {}

    @PostMapping("/{templateId}/activate")
    public String activateTemplate(
            @PathVariable String templateId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
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
    public String archiveTemplate(
            @PathVariable String templateId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
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
    public String deleteTemplate(
            @PathVariable String templateId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
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

    @Getter
    public static class TemplateForm {
        @Setter
        private String title;
        private String sectionsJson = "[]";

        public void setSectionsJson(String sectionsJson) {this.sectionsJson = (sectionsJson == null || sectionsJson.isBlank()) ? "[]" : sectionsJson;}
    }

    private record TemplatePresetResponse(String id, String name, java.util.List<PresetSection> sections,
                                          String renderedHtml) {}
}
