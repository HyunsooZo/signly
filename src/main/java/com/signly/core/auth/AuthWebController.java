package com.signly.core.auth;

import com.signly.core.auth.dto.LoginRequest;
import com.signly.core.auth.dto.LoginResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthWebController {

    private static final Logger logger = LoggerFactory.getLogger(AuthWebController.class);
    private final AuthService authService;

    public AuthWebController(AuthService authService) {
        this.authService = authService;
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
    public String logout(HttpServletResponse response, RedirectAttributes redirectAttributes) {
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
}