package com.signly.user.application;

import com.signly.user.application.dto.*;
import com.signly.user.application.mapper.UserDtoMapper;
import com.signly.common.exception.NotFoundException;
import com.signly.common.exception.ValidationException;
import com.signly.common.util.PasswordEncoder;
import com.signly.user.domain.model.*;
import com.signly.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDtoMapper userDtoMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserDtoMapper userDtoMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDtoMapper = userDtoMapper;
    }

    public UserResponse registerUser(RegisterUserCommand command) {
        Email email = Email.of(command.email());

        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("이미 사용 중인 이메일입니다");
        }

        Password password = Password.of(command.password());

        User user = User.create(
                email,
                password,
                command.name(),
                command.companyName(),
                command.userType(),
                passwordEncoder
        );

        User savedUser = userRepository.save(user);
        return userDtoMapper.toResponse(savedUser);
    }

    public UserResponse authenticateUser(LoginCommand command) {
        Email email = Email.of(command.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        if (!user.isActive()) {
            throw new ValidationException("비활성화된 사용자입니다");
        }

        Password password = Password.of(command.password());
        if (!user.validatePassword(password, passwordEncoder)) {
            throw new ValidationException("비밀번호가 일치하지 않습니다");
        }

        return userDtoMapper.toResponse(user);
    }

    public UserResponse updateProfile(String userId, UpdateProfileCommand command) {
        UserId userIdObj = UserId.of(userId);
        User user = userRepository.findById(userIdObj)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        user.updateProfile(command.name(), command.companyName());
        User updatedUser = userRepository.save(user);

        return userDtoMapper.toResponse(updatedUser);
    }

    public void changePassword(String userId, ChangePasswordCommand command) {
        UserId userIdObj = UserId.of(userId);
        User user = userRepository.findById(userIdObj)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        Password oldPassword = Password.of(command.oldPassword());
        Password newPassword = Password.of(command.newPassword());

        user.changePassword(oldPassword, newPassword, passwordEncoder);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(String userId) {
        UserId userIdObj = UserId.of(userId);
        User user = userRepository.findById(userIdObj)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        return userDtoMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        Email emailObj = Email.of(email);
        User user = userRepository.findByEmail(emailObj)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        return userDtoMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        Email emailObj = Email.of(email);
        return userRepository.existsByEmail(emailObj);
    }
}