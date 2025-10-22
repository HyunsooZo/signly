package com.signly.core.auth;

import com.signly.core.auth.dto.LoginRequest;
import com.signly.core.auth.dto.LoginResponse;
import com.signly.core.auth.dto.RefreshTokenRequest;
import com.signly.common.exception.UnauthorizedException;
import com.signly.common.security.JwtTokenProvider;
import com.signly.common.security.SecurityUser;
import com.signly.common.security.TokenRedisService;
import com.signly.user.domain.model.Email;
import com.signly.user.domain.model.Password;
import com.signly.user.domain.model.User;
import com.signly.user.domain.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRedisService tokenRedisService;

    public AuthService(AuthenticationManager authenticationManager,
                      JwtTokenProvider jwtTokenProvider,
                      PasswordEncoder passwordEncoder,
                      UserRepository userRepository,
                      TokenRedisService tokenRedisService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.tokenRedisService = tokenRedisService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            User user = securityUser.getUser();

            String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId().getValue(),
                user.getEmail().getValue(),
                user.getUserType().name()
            );

            String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getUserId().getValue()
            );

            // Redis에 토큰 저장
            tokenRedisService.saveAccessToken(user.getUserId().getValue(), accessToken);
            tokenRedisService.saveRefreshToken(user.getUserId().getValue(), refreshToken);

            var company = user.getCompany();
            return new LoginResponse(
                accessToken,
                refreshToken,
                user.getUserId().getValue(),
                user.getEmail().getValue(),
                user.getName(),
                company != null ? company.getName() : null,
                company != null ? company.getPhone() : null,
                company != null ? company.getAddress() : null,
                user.getUserType(),
                jwtTokenProvider.getAccessTokenValidityInMs()
            );

        } catch (AuthenticationException e) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다");
        }
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
            user.getUserId().getValue(),
            user.getEmail().getValue(),
            user.getUserType().name()
        );

        String newRefreshToken = jwtTokenProvider.createRefreshToken(
            user.getUserId().getValue()
        );

        // Redis에 새로운 토큰 저장
        tokenRedisService.saveAccessToken(user.getUserId().getValue(), newAccessToken);
        tokenRedisService.saveRefreshToken(user.getUserId().getValue(), newRefreshToken);

        var company = user.getCompany();
        return new LoginResponse(
            newAccessToken,
            newRefreshToken,
            user.getUserId().getValue(),
            user.getEmail().getValue(),
            user.getName(),
            company != null ? company.getName() : null,
            company != null ? company.getPhone() : null,
            company != null ? company.getAddress() : null,
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
    public void validateCredentials(String email, String password) {
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