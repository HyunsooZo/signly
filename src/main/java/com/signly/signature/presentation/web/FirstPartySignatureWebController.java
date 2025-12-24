package com.signly.signature.presentation.web;

import com.signly.common.exception.ValidationException;
import com.signly.common.security.CurrentUserProvider;
import com.signly.common.security.UserPrincipal;
import com.signly.common.web.BaseWebController;
import com.signly.signature.application.FirstPartySignatureService;
import com.signly.signature.application.dto.FirstPartySignatureResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class FirstPartySignatureWebController extends BaseWebController {

    private static final Logger logger = LoggerFactory.getLogger(FirstPartySignatureWebController.class);

    private final FirstPartySignatureService firstPartySignatureService;
    private final CurrentUserProvider currentUserProvider;
    private final com.signly.user.application.UserService userService;

    @GetMapping("/profile/info")
    public String viewSignature(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal UserPrincipal securityUser,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            model.addAttribute("pageTitle", "나의 정보");

            try {
                FirstPartySignatureResponse signature = firstPartySignatureService.getSignature(resolvedUserId);
                model.addAttribute("hasSignature", true);
                model.addAttribute("signature", signature);
                model.addAttribute("signatureDataUrl", firstPartySignatureService.getSignatureDataUrl(resolvedUserId));
            } catch (ValidationException e) {
                model.addAttribute("hasSignature", false);
            }

            // 현재 사용자 정보 추가 (프로필 수정 폼용)
            com.signly.user.application.dto.UserResponse currentUser = userService
                    .getUserByEmail(securityUser.getEmail());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("currentUserCompany",
                    currentUser.getCompanyName() != null ? BusinessResponse
                            .of(currentUser.getCompanyName(), currentUser.getBusinessPhone(), currentUser.getBusinessAddress())
                            : null);
            model.addAttribute("currentUserBusinessPhone", currentUser.getBusinessPhone());
            model.addAttribute("currentUserBusinessAddress", currentUser.getBusinessAddress());

            return "profile/profile";
        } catch (Exception e) {
            logger.error("서명 관리 페이지 로드 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "서명 정보를 불러오는 중 오류가 발생했습니다.");
            return "redirect:/home";
        }
    }

    @PostMapping("/profile/info")
    public String uploadSignature(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal UserPrincipal securityUser,
            HttpServletRequest request,
            @RequestParam("signatureData") String signatureData,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            firstPartySignatureService.uploadSignature(resolvedUserId, signatureData);
            redirectAttributes.addFlashAttribute("successMessage", "서명이 저장되었습니다.");
        } catch (ValidationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("서명 업로드 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "서명 업로드 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
        return "redirect:/profile/info";
    }
}
