package com.signly.core.auth;

import com.signly.common.exception.AccountLockedException;
import com.signly.user.application.AccountLockService;
import com.signly.user.application.dto.AccountUnlockRequest;
import com.signly.user.domain.model.AccountUnlockToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountLockWebController {

    private final AccountLockService accountLockService;

    @GetMapping("/unlock-account")
    public String showUnlockPage(
            @RequestParam(value = "token", required = false) String token,
            Model model) {
        
        if (token == null || token.trim().isEmpty()) {
            return "redirect:/login";
        }

        model.addAttribute("token", token);
        model.addAttribute("unlockRequest", new AccountUnlockRequest(token));
        
        return "auth/unlock-account";
    }

    @PostMapping("/unlock-account")
    public String unlockAccount(
            @Valid @ModelAttribute("unlockRequest") AccountUnlockRequest unlockRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "auth/unlock-account";
        }

        try {
            String ipAddress = getCurrentIpAddress();
            accountLockService.unlockAccount(unlockRequest.getToken(), ipAddress);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "계정이 성공적으로 잠금 해제되었습니다. 임시 비밀번호가 이메일로 발송되었습니다.");
            return "redirect:/unlock-account/success";
            
        } catch (AccountLockedException e) {
            bindingResult.reject("invalidToken", e.getMessage());
            return "auth/unlock-account";
        } catch (Exception e) {
            log.error("Account unlock failed", e);
            bindingResult.reject("unlockFailed", "계정 잠금 해제 중 오류가 발생했습니다.");
            return "auth/unlock-account";
        }
    }

    @GetMapping("/unlock-account/success")
    public String showUnlockSuccessPage() {
        return "auth/unlock-account-success";
    }

    @GetMapping("/account-locked")
    public String showAccountLockedPage() {
        return "auth/account-locked";
    }

    /**
     * 현재 요청의 IP 주소 추출
     */
    private String getCurrentIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.error("Failed to get IP address", e);
        }
        return "UNKNOWN";
    }
}