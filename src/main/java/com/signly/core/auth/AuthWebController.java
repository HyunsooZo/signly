package com.signly.core.auth;

import com.signly.common.email.EmailService;
import com.signly.common.security.SecurityUser;
import com.signly.core.auth.dto.LoginRequest;
import com.signly.core.auth.dto.LoginResponse;
import com.signly.user.application.UserService;
import com.signly.user.application.dto.UserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthWebController {

    private static final Logger logger = LoggerFactory.getLogger(AuthWebController.class);
    private final AuthService authService;
    private final UserService userService;
    private final EmailService emailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public AuthWebController(AuthService authService, UserService userService, EmailService emailService) {
        this.authService = authService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                       @RequestParam String password,
                       @RequestParam(required = false) boolean rememberMe,
                       HttpServletResponse response,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        try {
            LoginRequest request = new LoginRequest(email, password);
            LoginResponse loginResponse = authService.login(request);

            // JWT 토큰을 쿠키에 저장
            Cookie authCookie = new Cookie("authToken", loginResponse.accessToken());
            authCookie.setHttpOnly(true);
            authCookie.setSecure(false); // 개발환경에서는 false, 프로덕션에서는 true
            authCookie.setPath("/");
            authCookie.setMaxAge(rememberMe ? 7 * 24 * 60 * 60 : 24 * 60 * 60); // 기억하기 체크 시 7일, 아니면 1일
            response.addCookie(authCookie);

            logger.info("로그인 성공: {}", email);
            redirectAttributes.addFlashAttribute("successMessage", "로그인되었습니다.");
            return "redirect:/home";

        } catch (Exception e) {
            logger.warn("로그인 실패: {} - {}", email, e.getMessage());
            model.addAttribute("errorMessage", "이메일 또는 비밀번호가 올바르지 않습니다.");
            model.addAttribute("email", email);
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/logout")
    public String logout(@AuthenticationPrincipal SecurityUser securityUser,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        // Redis에서 토큰 삭제
        if (securityUser != null) {
            String userId = securityUser.getUser().getUserId().getValue();
            authService.logout(userId);
            logger.info("로그아웃 완료: userId={}", userId);
        }

        // 쿠키 삭제
        Cookie authCookie = new Cookie("authToken", "");
        authCookie.setHttpOnly(true);
        authCookie.setPath("/");
        authCookie.setMaxAge(0);
        response.addCookie(authCookie);

        redirectAttributes.addFlashAttribute("successMessage", "로그아웃되었습니다.");
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                 HttpServletRequest request,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            logger.info("비밀번호 재설정 요청: {}", email);

            // 토큰 생성
            String token = userService.generatePasswordResetToken(email);

            // 사용자 정보 가져오기
            UserResponse user = userService.getUserByEmail(email);

            // 이메일 발송
            emailService.sendPasswordResetEmail(email, user.name(), token, baseUrl);

            logger.info("비밀번호 재설정 이메일 발송 완료: {}", email);
            redirectAttributes.addFlashAttribute("successMessage",
                "비밀번호 재설정 링크를 이메일로 발송했습니다. 이메일을 확인해주세요.");
            return "redirect:/forgot-password";

        } catch (Exception e) {
            logger.error("비밀번호 재설정 요청 실패: {} - {}", email, e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", email);
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        try {
            // 토큰 유효성 검증
            String email = userService.getUserEmailByResetToken(token);
            model.addAttribute("token", token);
            model.addAttribute("email", email);
            return "auth/reset-password";
        } catch (Exception e) {
            logger.error("비밀번호 재설정 페이지 접근 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/reset-password-error";
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
            }

            userService.resetPassword(token, newPassword);

            logger.info("비밀번호 재설정 완료");
            redirectAttributes.addFlashAttribute("successMessage",
                "비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해주세요.");
            return "redirect:/login";

        } catch (Exception e) {
            logger.error("비밀번호 재설정 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
    }
}