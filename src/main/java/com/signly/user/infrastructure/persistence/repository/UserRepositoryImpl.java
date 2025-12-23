package com.signly.user.infrastructure.persistence.repository;

import com.signly.common.encryption.AesEncryptionService;
import com.signly.user.domain.model.Email;
import com.signly.user.domain.model.User;
import com.signly.user.domain.model.UserId;
import com.signly.user.domain.repository.UserRepository;
import com.signly.user.infrastructure.persistence.entity.UserEntity;
import com.signly.user.infrastructure.persistence.mapper.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserEntityMapper userEntityMapper;
    private final AesEncryptionService encryptionService;

    @Override
    public User save(User user) {
        Optional<UserEntity> existingEntity = userJpaRepository.findById(user.getUserId().value());

        if (existingEntity.isPresent()) {
            UserEntity entity = existingEntity.get();
            userEntityMapper.updateEntity(entity, user);
            UserEntity savedEntity = userJpaRepository.save(entity);
            return userEntityMapper.toDomain(savedEntity);
        } else {
            UserEntity entity = userEntityMapper.toEntity(user);
            UserEntity savedEntity = userJpaRepository.save(entity);
            return userEntityMapper.toDomain(savedEntity);
        }
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return userJpaRepository.findById(userId.value())
                .map(userEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        // 이메일 해시로 검색 (Blind Index 사용)
        String emailHash = encryptionService.hashEmail(email.value());
        return userJpaRepository.findByEmailHash(emailHash)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        // 이메일 해시로 중복 체크 (Blind Index 사용)
        String emailHash = encryptionService.hashEmail(email.value());
        return userJpaRepository.existsByEmailHash(emailHash);
    }

    @Override
    public Optional<User> findByVerificationToken(String token) {
        return userJpaRepository.findByVerificationToken(token)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByUnlockToken(String token) {
        return userJpaRepository.findByUnlockToken(token)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public void delete(User user) {
        userJpaRepository.deleteById(user.getUserId().value());
    }
}