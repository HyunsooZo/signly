package com.signly.user.presentation;

import com.signly.user.application.UserService;
import com.signly.user.application.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserCommand command) {
        UserResponse response = userService.registerUser(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "로그인", description = "사용자 인증을 수행합니다")
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginCommand command) {
        UserResponse response = userService.authenticateUser(command);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프로필 조회", description = "사용자 프로필을 조회합니다")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getProfile(@PathVariable String userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프로필 수정", description = "사용자 프로필을 수정합니다")
    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateProfileCommand command) {
        UserResponse response = userService.updateProfile(userId, command);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "비밀번호 변경", description = "사용자 비밀번호를 변경합니다")
    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String userId,
            @Valid @RequestBody ChangePasswordCommand command) {
        userService.changePassword(userId, command);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이메일 중복 확인", description = "이메일 중복 여부를 확인합니다")
    @GetMapping("/check-email")
    public ResponseEntity<CheckEmailResponse> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        CheckEmailResponse response = new CheckEmailResponse(exists);
        return ResponseEntity.ok(response);
    }

    public record CheckEmailResponse(boolean exists) {
    }
}