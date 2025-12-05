package com.signly.user.presentation.web;

import com.signly.common.exception.ValidationException;
import com.signly.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 이메일 인증 웹 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class EmailVerificationWebController {

    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationWebController.class);

    private final UserService userService;

    /**
     * 이메일 인증 처리 (멱등성 보장)
     * GET /verify-email?token={token}
     */
    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam String token,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.verifyEmail(token);

            // 멱등성: 이미 인증된 경우에도 성공 페이지로
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "이메일 인증이 완료되었습니다. 로그인해주세요."
            );
            return "redirect:/email-verified";

        } catch (ValidationException e) {
            logger.warn("이메일 인증 실패: token={}, error={}", token.substring(0, 8), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/email-verify-error";
        } catch (Exception e) {
            logger.error("이메일 인증 중 예상치 못한 오류 발생", e);
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "인증 처리 중 오류가 발생했습니다. 다시 시도해주세요."
            );
            return "redirect:/email-verify-error";
        }
    }

    /**
     * 이메일 인증 성공 페이지
     * GET /email-verified
     */
    @GetMapping("/email-verified")
    public String emailVerifiedPage() {
        return "auth/email-verified";
    }

    /**
     * 이메일 인증 실패 페이지
     * GET /email-verify-error
     */
    @GetMapping("/email-verify-error")
    public String emailVerifyErrorPage() {
        return "auth/email-verify-error";
    }

    /**
     * 회원가입 완료 페이지 (이메일 확인 안내)
     * GET /registration-pending
     */
    @GetMapping("/registration-pending")
    public String registrationPendingPage() {
        return "auth/registration-pending";
    }

    /**
     * 인증 이메일 재발송
     * POST /resend-verification
     */
    @PostMapping("/resend-verification")
    public String resendVerification(
            @RequestParam String email,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.resendVerificationEmail(email);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "인증 이메일을 재발송했습니다. 이메일을 확인해주세요."
            );
            return "redirect:/registration-pending";

        } catch (Exception e) {
            logger.error("인증 이메일 재발송 실패: email={}", email, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/email-verify-error";
        }
    }
}
