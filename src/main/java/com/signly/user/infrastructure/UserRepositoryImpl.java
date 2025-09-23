package com.signly.user.infrastructure.repository.impl;

import com.signly.user.domain.model.Email;
import com.signly.user.domain.model.User;
import com.signly.user.domain.model.UserId;
import com.signly.user.domain.repository.UserRepository;
import com.signly.user.infrastructure.entity.UserEntity;
import com.signly.user.infrastructure.mapper.UserEntityMapper;
import com.signly.user.infrastructure.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserEntityMapper userEntityMapper;

    public UserRepositoryImpl(UserJpaRepository userJpaRepository, UserEntityMapper userEntityMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userEntityMapper = userEntityMapper;
    }

    @Override
    public User save(User user) {
        Optional<UserEntity> existingEntity = userJpaRepository.findById(user.getUserId().getValue());

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
        return userJpaRepository.findById(userId.getValue())
                .map(userEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return userJpaRepository.findByEmail(email.getValue())
                .map(userEntityMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.getValue());
    }

    @Override
    public void delete(User user) {
        userJpaRepository.deleteById(user.getUserId().getValue());
    }
}