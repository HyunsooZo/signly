package com.signly.security.service;

import com.signly.security.dto.LoginRequest;
import com.signly.security.dto.LoginResponse;
import com.signly.security.dto.RefreshTokenRequest;
import com.signly.security.dto.TokenResponse;
import com.signly.security.jwt.JwtTokenProvider;
import com.signly.user.application.UserService;
import com.signly.user.application.dto.LoginCommand;
import com.signly.user.application.dto.UserResponse;
import com.signly.common.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuthenticationService {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    public AuthenticationService(UserService userService, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        LoginCommand command = new LoginCommand(request.email(), request.password());
        UserResponse user = userService.authenticateUser(command);

        List<String> roles = List.of(user.userType().toString());
        String accessToken = tokenProvider.createAccessToken(user.userId(), user.email(), roles);
        String refreshToken = tokenProvider.createRefreshToken(user.userId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.userId(),
                user.email(),
                user.name(),
                user.userType()
        );
    }

    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다");
        }

        String userId = tokenProvider.getUserIdFromToken(refreshToken);
        UserResponse user = userService.getUserById(userId);

        List<String> roles = List.of(user.userType().toString());
        String newAccessToken = tokenProvider.createAccessToken(user.userId(), user.email(), roles);
        String newRefreshToken = tokenProvider.createRefreshToken(user.userId());

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public void logout(String accessToken) {
        // In a production system, you would typically:
        // 1. Add the token to a blacklist stored in Redis
        // 2. Remove refresh token from database if stored
        // For now, we'll just validate the token format
        if (!tokenProvider.validateToken(accessToken) || !tokenProvider.isAccessToken(accessToken)) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다");
        }
    }
}