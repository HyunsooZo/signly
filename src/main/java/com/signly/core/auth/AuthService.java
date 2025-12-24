package com.signly.core.auth;

import com.signly.common.exception.AccountLockedException;
import com.signly.common.exception.UnauthorizedException;
import com.signly.common.security.JwtTokenProvider;
import com.signly.common.security.TokenRedisService;
import com.signly.core.auth.dto.LoginRequest;
import com.signly.core.auth.dto.LoginResponse;
import com.signly.core.auth.dto.RefreshTokenRequest;
import com.signly.user.application.LoginAttemptService;
import com.signly.user.domain.model.Email;
import com.signly.user.domain.model.Password;
import com.signly.user.domain.model.User;
import com.signly.user.domain.model.UserStatus;
import com.signly.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRedisService tokenRedisService;
    private final LoginAttemptService loginAttemptService;

    public LoginResponse login(LoginRequest request) {
        String email = request.email();
        String ipAddress = getCurrentIpAddress();

        // 1. 계정 잠금 여부 확인
        if (loginAttemptService.isAccountLocked(email)) {
            log.warn("Login attempt for locked account: {}", email);
            throw new AccountLockedException("계정이 잠겨있습니다. 이메일을 확인해주세요.");
        }

        try {
            var token = new UsernamePasswordAuthenticationToken(email, request.password());
            var authentication = authenticationManager.authenticate(token);

            User user = userRepository.findByEmail(Email.of(email))
                    .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다"));

            // 2. 로그인 성공 - 실패 횟수 초기화
            loginAttemptService.resetAttempts(email);

            String accessToken = jwtTokenProvider.createAccessToken(
                    user.getUserId().value(),
                    user.getEmail().value(),
                    user.getUserType().name()
            );

            String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId().value());

            // Redis에 토큰 저장
            tokenRedisService.saveAccessToken(user.getUserId().value(), accessToken);
            tokenRedisService.saveRefreshToken(user.getUserId().value(), refreshToken);

            log.info("Login successful for user: {}", user.getUserId().value());

            var company = user.getCompany();
            return new LoginResponse(
                    accessToken,
                    refreshToken,
                    user.getUserId().value(),
                    user.getEmail().value(),
                    user.getName(),
                    company != null ? company.name() : null,
                    company != null ? company.phone() : null,
                    company != null ? company.address() : null,
                    user.getUserType(),
                    jwtTokenProvider.getAccessTokenValidityInMs()
            );

        } catch (DisabledException e) {
            // 계정 비활성화 - PENDING, SUSPENDED, LOCKED 상태 확인
            User user = userRepository.findByEmail(Email.of(email)).orElse(null);
            if (user != null && user.getStatus() == UserStatus.PENDING) {
                throw new UnauthorizedException("이메일 인증을 완료해주세요");
            } else if (user != null && user.getStatus() == UserStatus.SUSPENDED) {
                throw new UnauthorizedException("정지된 계정입니다. 관리자에게 문의하세요.");
            } else if (user != null && user.getStatus() == UserStatus.LOCKED) {
                throw new AccountLockedException("계정이 잠겨있습니다. 이메일을 확인해주세요.");
            } else {
                throw new UnauthorizedException("비활성화된 계정입니다");
            }
        } catch (BadCredentialsException e) {
            // 3. 로그인 실패 - 실패 횟수 증가 (5회 시 자동 잠금)
            loginAttemptService.recordFailedAttempt(email, ipAddress);

            int remaining = loginAttemptService.getRemainingAttempts(email);
            if (remaining > 0) {
                log.warn("Login failed for email: {} (remaining attempts: {})", email, remaining);
                throw new UnauthorizedException(
                        String.format("이메일 또는 비밀번호가 올바르지 않습니다. (남은 시도: %d회)", remaining)
                );
            } else {
                log.warn("Account locked due to 5 failed login attempts: {}", email);
                throw new AccountLockedException(
                        "로그인 5회 실패로 계정이 잠겼습니다. 이메일을 확인해주세요."
                );
            }
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다");
        }
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

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtTokenProvider.isTokenValid(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다");
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // Redis에서 리프레시 토큰 검증
        if (!tokenRedisService.isRefreshTokenValid(userId, refreshToken)) {
            throw new UnauthorizedException("Redis에 저장된 리프레시 토큰과 일치하지 않습니다");
        }

        User user = userRepository.findById(com.signly.user.domain.model.UserId.of(userId))
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다"));

        if (!user.isActive()) {
            throw new UnauthorizedException("비활성화된 사용자입니다");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getUserId().value(),
                user.getEmail().value(),
                user.getUserType().name()
        );

        String newRefreshToken = jwtTokenProvider.createRefreshToken(
                user.getUserId().value()
        );

        // Redis에 새로운 토큰 저장
        tokenRedisService.saveAccessToken(user.getUserId().value(), newAccessToken);
        tokenRedisService.saveRefreshToken(user.getUserId().value(), newRefreshToken);

        var company = user.getCompany();
        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                user.getUserId().value(),
                user.getEmail().value(),
                user.getName(),
                company != null ? company.name() : null,
                company != null ? company.phone() : null,
                company != null ? company.address() : null,
                user.getUserType(),
                jwtTokenProvider.getAccessTokenValidityInMs()
        );
    }

    /**
     * 로그아웃 - Redis에서 토큰 삭제
     */
    public void logout(String userId) {
        tokenRedisService.deleteAllTokens(userId);
    }

    @Transactional(readOnly = true)
    public void validateCredentials(
            String email,
            String password
    ) {
        User user = userRepository.findByEmail(Email.of(email))
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다"));

        // passwordEncoder 빈을 주입받아 사용
        if (!user.validatePassword(Password.of(password), passwordEncoder)) {
            throw new UnauthorizedException("비밀번호가 올바르지 않습니다");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("비활성화된 사용자입니다");
        }
    }
}