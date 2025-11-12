package com.signly.user.presentation.web;

import com.signly.common.exception.BusinessException;
import com.signly.common.exception.ValidationException;
import com.signly.common.util.PasswordValidator;
import com.signly.user.application.UserService;
import com.signly.user.application.dto.RegisterUserCommand;
import com.signly.user.domain.model.UserType;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class UserWebController {

    private static final Logger logger = LoggerFactory.getLogger(UserWebController.class);
    private final UserService userService;

    public UserWebController(UserService userService) {
        this.userService = userService;
    }



    @PostMapping("/register")
    public String processRegistration(@RequestParam String email,
                                    @RequestParam String password,
                                    @RequestParam String confirmPassword,
                                    @RequestParam String name,
                                    @RequestParam(required = false) String companyName,
                                    @RequestParam(required = false) String businessPhone,
                                    @RequestParam(required = false) String businessAddress,
                                    @RequestParam String userType,
                                    @RequestParam(required = false) boolean agreeTerms,
                                    Model model,
                                    RedirectAttributes redirectAttributes,
                                    HttpServletRequest request) {
        try {
            // 기본 유효성 검사
            Map<String, String> fieldErrors = new HashMap<>();

            if (email == null || email.trim().isEmpty()) {
                fieldErrors.put("email", "이메일은 필수입니다.");
            } else if (!isValidEmail(email)) {
                fieldErrors.put("email", "올바른 이메일 형식이 아닙니다.");
            }

            if (!PasswordValidator.hasMinimumLength(password)) {
                fieldErrors.put("password", "비밀번호는 최소 " + PasswordValidator.MIN_PASSWORD_LENGTH + "자 이상이어야 합니다.");
            } else if (!PasswordValidator.isValid(password)) {
                fieldErrors.put("password", PasswordValidator.PASSWORD_REQUIREMENT_MESSAGE);
            }

            if (!password.equals(confirmPassword)) {
                fieldErrors.put("confirmPassword", "비밀번호가 일치하지 않습니다.");
            }

            if (name == null || name.trim().isEmpty()) {
                fieldErrors.put("name", "이름은 필수입니다.");
            }

            if (userType == null || userType.trim().isEmpty()) {
                fieldErrors.put("userType", "사용자 유형을 선택해주세요.");
            }

            if (!agreeTerms) {
                model.addAttribute("errorMessage", "이용약관에 동의해주세요.");
                model.addAttribute("fieldErrors", fieldErrors);
                return "auth/register";
            }

            if (!fieldErrors.isEmpty()) {
                model.addAttribute("fieldErrors", fieldErrors);
                return "auth/register";
            }

            // 회원가입 처리
            RegisterUserCommand command = new RegisterUserCommand(
                email.trim(),
                password,
                name.trim(),
                companyName != null ? companyName.trim() : null,
                businessPhone != null ? businessPhone.trim() : null,
                businessAddress != null ? businessAddress.trim() : null,
                UserType.valueOf(userType)
            );

            userService.registerUser(command);

            logger.info("사용자 회원가입 성공: {}", email);
            redirectAttributes.addFlashAttribute("successMessage",
                "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";

        } catch (ValidationException e) {
            logger.warn("회원가입 유효성 검사 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";

        } catch (BusinessException e) {
            logger.warn("회원가입 비즈니스 로직 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";

        } catch (Exception e) {
            logger.error("회원가입 처리 중 예상치 못한 오류 발생", e);
            model.addAttribute("errorMessage", "회원가입 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "auth/register";
        }
    }



    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static class RegistrationForm {
        private String email;
        private String password;
        private String confirmPassword;
        private String name;
        private String companyName;
        private String userType;
        private boolean agreeTerms;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }
        public boolean isAgreeTerms() { return agreeTerms; }
        public void setAgreeTerms(boolean agreeTerms) { this.agreeTerms = agreeTerms; }
    }
}