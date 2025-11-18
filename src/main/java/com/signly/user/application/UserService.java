package com.signly.user.application;

import com.signly.common.exception.NotFoundException;
import com.signly.common.exception.ValidationException;
import com.signly.user.application.dto.RegisterUserCommand;
import com.signly.user.application.dto.UserResponse;
import com.signly.user.application.mapper.UserDtoMapper;
import com.signly.user.domain.model.Company;
import com.signly.user.domain.model.Email;
import com.signly.user.domain.model.Password;
import com.signly.user.domain.model.User;
import com.signly.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDtoMapper userDtoMapper;

    // 비밀번호 재설정 토큰 저장소 (실제 운영환경에서는 Redis나 DB 사용 권장)
    private final Map<String, PasswordResetToken> resetTokens = new ConcurrentHashMap<>();

    private record PasswordResetToken(String email, LocalDateTime expiryTime) {
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

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

        var savedUser = userRepository.save(user);
        return userDtoMapper.toResponse(savedUser);
    }


    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        var emailObj = Email.of(email);
        var user = userRepository.findByEmail(emailObj)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

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
    }

    @Transactional(readOnly = true)
    public String getUserEmailByResetToken(String token) {
        var resetToken = resetTokens.get(token);

        if (resetToken == null || resetToken.isExpired()) {
            throw new ValidationException("유효하지 않거나 만료된 비밀번호 재설정 링크입니다");
        }

        return resetToken.email();
    }
}
