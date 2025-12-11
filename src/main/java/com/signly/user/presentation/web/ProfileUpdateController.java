package com.signly.user.presentation.web;

import com.signly.common.exception.ValidationException;
import com.signly.common.security.CurrentUserProvider;
import com.signly.common.security.SecurityUser;
import com.signly.common.web.BaseWebController;
import com.signly.user.application.UserService;
import com.signly.user.application.dto.UpdateUserCommand;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileUpdateController extends BaseWebController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileUpdateController.class);
    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            @RequestParam String name,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String businessPhone,
            @RequestParam(required = false) String businessAddress,
            RedirectAttributes redirectAttributes) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);

            UpdateUserCommand command = new UpdateUserCommand(
                    resolvedUserId,
                    name,
                    companyName,
                    businessPhone,
                    businessAddress);

            userService.updateUser(command);

            redirectAttributes.addFlashAttribute("successMessage", "프로필 정보가 저장되었습니다.");
            return "redirect:/profile/info";

        } catch (ValidationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile/info";
        } catch (Exception e) {
            logger.error("프로필 수정 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "프로필 수정 중 오류가 발생했습니다.");
            return "redirect:/profile/info";
        }
    }
}
