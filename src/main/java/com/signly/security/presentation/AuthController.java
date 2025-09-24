package com.signly.security.presentation;

import com.signly.security.dto.LoginRequest;
import com.signly.security.dto.LoginResponse;
import com.signly.security.dto.RefreshTokenRequest;
import com.signly.security.dto.TokenResponse;
import com.signly.security.service.AuthenticationService;
import com.signly.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "사용자 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "사용자 로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "로그인 성공"),
            @SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
            @SwaggerApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
            @SwaggerApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "로그아웃", description = "현재 사용자를 로그아웃합니다.")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @SwaggerApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.substring("Bearer ".length());
        authenticationService.logout(token);
        return ResponseEntity.ok(ApiResponse.success());
    }
}