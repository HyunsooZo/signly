package com.signly.user.application;

import com.signly.common.audit.aop.Auditable;
import com.signly.common.audit.domain.model.AuditAction;
import com.signly.common.audit.domain.model.EntityType;
import com.signly.common.exception.NotFoundException;
import com.signly.common.exception.ValidationException;
import com.signly.user.application.dto.ChangePasswordCommand;
import com.signly.user.application.dto.RegisterUserCommand;
import com.signly.user.application.dto.UserResponse;
import com.signly.user.application.mapper.UserDtoMapper;
import com.signly.user.domain.model.Company;
import com.signly.user.domain.model.Email;
import com.signly.user.domain.model.EncodedPassword;
import com.signly.user.domain.model.Password;
import com.signly.user.domain.model.User;
import com.signly.user.domain.model.UserId;
import com.signly.user.domain.repository.UserRepository;
import com.signly.user.application.PasswordHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDtoMapper userDtoMapper;
    private final PasswordHistoryService passwordHistoryService;
    private final com.signly.notification.application.EmailNotificationService emailNotificationService;

    // 비밀번호 재설정 토큰 저장소 (실제 운영환경에서는 Redis나 DB 사용 권장)
    private final Map<String, PasswordResetToken> resetTokens = new ConcurrentHashMap<>();

    private record PasswordResetToken(String email, LocalDateTime expiryTime) {
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    @Auditable(
            entityType = EntityType.USER,
            action = AuditAction.USER_REGISTERED,
            entityIdParam = "#result.userId"
    )
    public UserResponse registerUser(RegisterUserCommand command) {
        var email = Email.of(command.email());

        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("이미 사용 중인 이메일입니다");
        }

        var password = Password.of(command.password());
        var company = Company.of(
                command.companyName(),
                command.businessPhone(),
                command.businessAddress()
        );

        var user = User.create(
                email,
                password,
                command.name(),
                company,
                command.userType(),
                passwordEncoder
        );

        // 이메일 인증 토큰 생성
        var token = user.generateVerificationToken();

        var savedUser = userRepository.save(user);

        // 인증 이메일 발송
        emailNotificationService.sendEmailVerification(
                savedUser.getEmail().value(),
                savedUser.getName(),
                token.getValue()
        );

        return userDtoMapper.toResponse(savedUser);
    }

    /**
     * 이메일로 사용자 조회
     * 캐싱 제거: 사용자 정보는 자주 변경되고 민감하므로 캐싱하지 않음
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        var emailObj = Email.of(email);
        var user = userRepository.findByEmail(emailObj)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        log.debug("Loaded user from DB: {}", email);
        return userDtoMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        var emailObj = Email.of(email);
        return userRepository.existsByEmail(emailObj);
    }

    public String generatePasswordResetToken(String email) {
        var emailObj = Email.of(email);
        var user = userRepository.findByEmail(emailObj)
                .orElseThrow(() -> new NotFoundException("해당 이메일로 등록된 사용자가 없습니다"));

        // 토큰 생성 (24시간 유효)
        var token = UUID.randomUUID().toString();
        var expiryTime = LocalDateTime.now().plusHours(24);

        resetTokens.put(token, new PasswordResetToken(email, expiryTime));

        return token;
    }

    public void resetPassword(
            String token,
            String newPassword
    ) {
        var resetToken = resetTokens.get(token);

        if (resetToken == null) {
            throw new ValidationException("유효하지 않은 비밀번호 재설정 링크입니다");
        }

        if (resetToken.isExpired()) {
            resetTokens.remove(token);
            throw new ValidationException("비밀번호 재설정 링크가 만료되었습니다");
        }

        var emailObj = Email.of(resetToken.email());
        var user = userRepository.findByEmail(emailObj)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        var password = Password.of(newPassword);
        user.resetPassword(password, passwordEncoder);
        userRepository.save(user);

        // 토큰 삭제
        resetTokens.remove(token);
        log.info("Reset password for user: {} (cache evicted)", resetToken.email());
    }

    @Transactional(readOnly = true)
    public String getUserEmailByResetToken(String token) {
        var resetToken = resetTokens.get(token);

        if (resetToken == null || resetToken.isExpired()) {
            throw new ValidationException("유효하지 않거나 만료된 비밀번호 재설정 링크입니다");
        }

        return resetToken.email();
    }

    public void verifyEmail(String token) {
        var user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ValidationException("유효하지 않은 인증 토큰입니다"));

        // 도메인 로직에서 멱등성 처리
        user.verifyEmail(token);

        var savedUser = userRepository.save(user);

        log.info("Email verified for user: {}", savedUser.getEmail().value());
    }

    public void resendVerificationEmail(String email) {
        var emailObj = Email.of(email);
        var user = userRepository.findByEmail(emailObj)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        if (user.isEmailVerified()) {
            throw new ValidationException("이미 인증된 사용자입니다");
        }

        // 새 토큰 생성
        var token = user.generateVerificationToken();
        userRepository.save(user);

        // 인증 이메일 재발송
        emailNotificationService.sendEmailVerification(
                user.getEmail().value(),
                user.getName(),
                token.getValue());
    }

    public void updateUser(com.signly.user.application.dto.UpdateUserCommand command) {
        var user = userRepository.findById(com.signly.user.domain.model.UserId.of(command.userId()))
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        var company = Company.of(
                command.companyName(),
                command.businessPhone(),
                command.businessAddress()
        );

        user.updateProfile(command.name(), company);
        var savedUser = userRepository.save(user);

        log.info("Updated user profile: {}", savedUser.getEmail().value());

        userDtoMapper.toResponse(user);
    }

    /**
     * 비밀번호 변경
     * - 기존 비밀번호 확인
     * - 90일 이내 3개 비밀번호 재사용 방지
     *
     * @param userId 사용자 ID
     * @param command 비밀번호 변경 요청
     */
    @Auditable(
        entityType = EntityType.USER,
        action = AuditAction.USER_PASSWORD_CHANGED,
        entityIdParam = "#userId"
    )
    public void changePassword(String userId, ChangePasswordCommand command) {
        var user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        if (!user.isActive()) {
            throw new ValidationException("활성화된 사용자만 비밀번호를 변경할 수 있습니다");
        }

        // 기존 비밀번호 확인
        var oldPassword = Password.of(command.oldPassword());
        if (!user.validatePassword(oldPassword, passwordEncoder)) {
            throw new ValidationException("기존 비밀번호가 일치하지 않습니다");
        }

        // 새 비밀번호 유효성 검증
        var newPassword = Password.of(command.newPassword());

        // 90일 이내 3개 비밀번호 재사용 방지 검증
        passwordHistoryService.validatePasswordReuse(user.getUserId(), newPassword);

        // 기존 암호화된 비밀번호 저장 (이력용)
        var oldEncodedPassword = user.getEncodedPassword();

        // 비밀번호 변경
        user.changePassword(oldPassword, newPassword, passwordEncoder, passwordHistoryService);
        var savedUser = userRepository.save(user);

        // 이력 저장 (변경 후의 암호화된 비밀번호)
        var newEncodedPassword = EncodedPassword.of(savedUser.getEncodedPassword());
        passwordHistoryService.savePasswordHistory(
                user.getUserId(),
                newEncodedPassword,
                getCurrentIpAddress(),
                null  // User-Agent는 HttpServletRequest에서 추출
        );

        log.info("Password changed successfully for user: {}", savedUser.getEmail().value());
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
