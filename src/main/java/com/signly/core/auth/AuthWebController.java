package com.signly.core.auth;

import com.signly.common.email.EmailService;
import com.signly.common.security.SecurityUser;
import com.signly.common.security.TokenRedisService;
import com.signly.core.auth.dto.LoginRequest;
import com.signly.core.auth.dto.LoginResponse;
import com.signly.user.application.UserService;
import com.signly.user.application.dto.UserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.Arrays;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthWebController {

    private static final Logger logger = LoggerFactory.getLogger(AuthWebController.class);
    private final AuthService authService;
    private final UserService userService;
    private final EmailService emailService;
    private final TokenRedisService tokenRedisService;
    private final Environment environment;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public AuthWebController(
            AuthService authService,
            UserService userService,
            EmailService emailService,
            TokenRedisService tokenRedisService,
            Environment environment
    ) {
        this.authService = authService;
        this.userService = userService;
        this.emailService = emailService;
        this.tokenRedisService = tokenRedisService;
        this.environment = environment;
    }

    @GetMapping("/login")
    public String loginForm(
            @RequestParam(required = false) String returnUrl,
            @RequestParam(required = false) String error,
            Model model
    ) {
        if (returnUrl != null) {
            model.addAttribute("returnUrl", returnUrl);
        }
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute LoginRequest loginRequest,
            BindingResult bindingResult,
            @RequestParam(required = false, defaultValue = "false") boolean rememberMe,
            @RequestParam(required = false) String returnUrl,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // 유효성 검증 실패 시
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", bindingResult.getFieldError().getDefaultMessage());
            model.addAttribute("email", loginRequest.email());
            if (returnUrl != null) {
                model.addAttribute("returnUrl", returnUrl);
            }
            return "auth/login";
        }

        try {
            LoginResponse loginResponse = authService.login(loginRequest);

            // 환경별 보안 설정
            boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");
            
            // JWT 액세스 토큰을 쿠키에 저장 (보안 강화)
            Cookie authCookie = new Cookie("authToken", loginResponse.accessToken());
            authCookie.setHttpOnly(true); // XSS 방어를 위해 JavaScript 접근 차단
            authCookie.setSecure(isProduction); // 프로덕션에서만 HTTPS 강제
            authCookie.setPath("/");
            // SameSite는 Servlet 4.0+에서 지원, 하위 버전에서는 Response Header로 처리
            if (isProduction) {
                response.setHeader("Set-Cookie", 
                    String.format("%s; Path=/; HttpOnly; Secure; SameSite=Strict", 
                        authCookie.getName() + "=" + authCookie.getValue()));
            }
            authCookie.setMaxAge(60 * 60); // 1시간
            response.addCookie(authCookie);

            // 자동 로그인 체크 시에만 리프레시 토큰을 쿠키에 저장
            if (rememberMe) {
                Cookie refreshCookie = new Cookie("refreshToken", loginResponse.refreshToken());
                refreshCookie.setHttpOnly(true); // XSS 방어
                refreshCookie.setSecure(isProduction); // 프로덕션에서만 HTTPS 강제
                refreshCookie.setPath("/");
                // SameSite는 Servlet 4.0+에서 지원, 하위 버전에서는 Response Header로 처리
                if (isProduction) {
                    response.setHeader("Set-Cookie", 
                        String.format("%s; Path=/; HttpOnly; Secure; SameSite=Strict", 
                            refreshCookie.getName() + "=" + refreshCookie.getValue()));
                }
                refreshCookie.setMaxAge(30 * 24 * 60 * 60); // 30일
                response.addCookie(refreshCookie);
                logger.info("자동 로그인 활성화: {}", loginRequest.email());
            } else {
                logger.info("자동 로그인 비활성화: {}", loginRequest.email());
            }

            // Redis에 액세스 토큰 저장
            tokenRedisService.saveAccessToken(loginResponse.userId(), loginResponse.accessToken());
            
            logger.info("로그인 성공: {}", loginRequest.email());
            redirectAttributes.addFlashAttribute("successMessage", "로그인되었습니다.");

            // returnUrl이 있으면 해당 페이지로, 없으면 /home으로 리다이렉트
            String redirectUrl = (returnUrl != null && !returnUrl.isEmpty()) ? returnUrl : "/home";
            return "redirect:" + redirectUrl;

        } catch (Exception e) {
            logger.warn("로그인 실패: {} - {}", loginRequest.email(), e.getMessage());
            model.addAttribute("errorMessage", "이메일 또는 비밀번호가 올바르지 않습니다.");
            model.addAttribute("email", loginRequest.email());
            if (returnUrl != null) {
                model.addAttribute("returnUrl", returnUrl);
            }
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/logout")
    public String logout(
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        // Redis에서 토큰 삭제
        if (securityUser != null) {
            String userId = securityUser.getUser().getUserId().getValue();
            authService.logout(userId);
            logger.info("로그아웃 완료: userId={}", userId);
        }

        // 쿠키 삭제
        Cookie authCookie = new Cookie("authToken", "");
        authCookie.setHttpOnly(false);
        authCookie.setPath("/");
        authCookie.setMaxAge(0);
        response.addCookie(authCookie);

        Cookie refreshCookie = new Cookie("refreshToken", "");
        refreshCookie.setHttpOnly(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);

        redirectAttributes.addFlashAttribute("successMessage", "로그아웃되었습니다.");
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestParam String email,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
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
    public String resetPasswordForm(
            @RequestParam String token,
            Model model
    ) {
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
    public String resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
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