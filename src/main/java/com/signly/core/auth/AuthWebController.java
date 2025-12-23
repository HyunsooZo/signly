package com.signly.core.auth;

import com.signly.common.email.EmailService;
import com.signly.common.exception.AccountLockedException;
import com.signly.common.exception.UnauthorizedException;
import com.signly.common.security.SecurityUser;
import com.signly.common.security.TokenRedisService;
import com.signly.core.auth.dto.LoginRequest;
import com.signly.core.auth.dto.LoginResponse;
import com.signly.user.application.LoginAttemptService;
import com.signly.user.application.UserService;
import com.signly.user.application.dto.ChangePasswordCommand;
import com.signly.user.domain.model.Email;
import com.signly.user.domain.model.User;
import com.signly.user.domain.model.UserStatus;
import com.signly.user.domain.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private static final Logger logger = LoggerFactory.getLogger(AuthWebController.class);
    private final AuthService authService;
    private final UserService userService;
    private final EmailService emailService;
    private final TokenRedisService tokenRedisService;
    private final Environment environment;
    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

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
                response.setHeader("Set-Cookie", String.format("%s; Path=/; HttpOnly; Secure; SameSite=Strict", authCookie.getName() + "=" + authCookie.getValue()));
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

        } catch (DisabledException e) {
            logger.warn("로그인 실패: {} - 계정 비활성화", loginRequest.email());

            // PENDING 상태인지 확인
            try {
                User user = userRepository.findByEmail(Email.of(loginRequest.email())).orElse(null);
                if (user != null && user.getStatus() == UserStatus.PENDING) {
                    model.addAttribute("errorMessage", "이메일 인증을 완료해주세요");
                    model.addAttribute("showResendButton", true);
                    model.addAttribute("isPendingUser", true);
                } else if (user != null && user.getStatus() == UserStatus.SUSPENDED) {
                    model.addAttribute("errorMessage", "정지된 계정입니다. 관리자에게 문의하세요.");
                } else {
                    model.addAttribute("errorMessage", "비활성화된 계정입니다");
                }
            } catch (Exception ex) {
                model.addAttribute("errorMessage", "로그인 중 오류가 발생했습니다");
            }

            model.addAttribute("email", loginRequest.email());
            if (returnUrl != null) {
                model.addAttribute("returnUrl", returnUrl);
            }
            return "auth/login";
        } catch (AccountLockedException e) {
            logger.warn("로그인 실패: {} - {}", loginRequest.email(), e.getMessage());
            
            // 계정 잠금 상태
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isAccountLocked", true);
            model.addAttribute("email", loginRequest.email());
            
            // 남은 로그인 시도 횟수는 0
            model.addAttribute("remainingAttempts", 0);
            
            if (returnUrl != null) {
                model.addAttribute("returnUrl", returnUrl);
            }
            return "auth/login";
        } catch (UnauthorizedException e) {
            logger.warn("로그인 실패: {} - {}", loginRequest.email(), e.getMessage());

            // 이메일 인증 필요 메시지 체크
            if (e.getMessage() != null && e.getMessage().contains("이메일 인증")) {
                model.addAttribute("errorMessage", e.getMessage());
                model.addAttribute("showResendButton", true);
                model.addAttribute("isPendingUser", true);
            } else {
                model.addAttribute("errorMessage", e.getMessage());
                
                // 남은 로그인 시도 횟수 추가
                int remainingAttempts = loginAttemptService.getRemainingAttempts(loginRequest.email());
                if (remainingAttempts > 0 && remainingAttempts < 5) {
                    model.addAttribute("remainingAttempts", remainingAttempts);
                }
            }

            model.addAttribute("email", loginRequest.email());
            if (returnUrl != null) {
                model.addAttribute("returnUrl", returnUrl);
            }
            return "auth/login";
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

    @GetMapping("/logout")
    public String logout(
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        // Redis에서 토큰 삭제
        if (securityUser != null) {
            String userId = securityUser.getUser().getUserId().value();
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
            var user = userService.getUserByEmail(email);

            // 이메일 발송
            emailService.sendPasswordResetEmail(email, user.getName(), token, baseUrl);

            logger.info("비밀번호 재설정 이메일 발송 완료: {}", email);
            redirectAttributes.addFlashAttribute("successMessage", "비밀번호 재설정 링크를 이메일로 발송했습니다. 이메일을 확인해주세요.");
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

    @PostMapping("/resend-unlock-email")
    public String resendUnlockEmail(
            @RequestParam String email,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            logger.info("잠금 해제 이메일 재전송 요청: {}", email);

            // 사용자가 존재하고 잠긴 상태인지 확인
            User user = userRepository.findByEmail(Email.of(email))
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            if (user.getStatus() != UserStatus.LOCKED) {
                throw new IllegalArgumentException("잠긴 계정이 아닙니다");
            }

            // 잠금 해제 이메일 재전송
            loginAttemptService.resendUnlockEmail(email, baseUrl);

            logger.info("잠금 해제 이메일 재전송 완료: {}", email);
            redirectAttributes.addFlashAttribute("successMessage", 
                "잠금 해제 링크를 이메일로 재전송했습니다. 이메일을 확인해주세요.");
            return "redirect:/account-locked";

        } catch (Exception e) {
            logger.error("잠금 해제 이메일 재전송 실패: {} - {}", email, e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", email);
            return "auth/account-locked";
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

    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "auth/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute ChangePasswordCommand command,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/change-password";
        }

        try {
            userService.changePassword(securityUser.getUser().getUserId().value(), command);
            logger.info("Password changed for user: {}", securityUser.getUser().getUserId().value());
            redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다.");
            return "redirect:/profile";
        } catch (Exception e) {
            logger.error("Password change failed for user: {}", securityUser.getUser().getUserId().value(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/change-password";
        }
    }

    @GetMapping("/api/profile-status")
    @ResponseBody
    public Map<String, Object> getProfileStatus(@AuthenticationPrincipal SecurityUser securityUser) {
        Map<String, Object> response = new HashMap<>();

        if (securityUser != null) {
            User user = securityUser.getUser();
            boolean isProfileComplete = user.isProfileComplete();

            response.put("isProfileComplete", isProfileComplete);
            response.put("userName", user.getName());
            response.put("userEmail", user.getEmail().value());

            // Check what's missing for profile completion
            if (!isProfileComplete) {
                Map<String, String> missingFields = new HashMap<>();
                if (user.getCompany() == null || user.getCompany().name() == null || user.getCompany().name().trim().isEmpty()) {
                    missingFields.put("company", "회사 정보");
                }
                response.put("missingFields", missingFields);
            }
        } else {
            response.put("isProfileComplete", false);
            response.put("error", "User not authenticated");
        }

        return response;
    }
}