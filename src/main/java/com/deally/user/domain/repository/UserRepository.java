package com.deally.user.domain.repository;

import com.deally.user.domain.model.Email;
import com.deally.user.domain.model.User;
import com.deally.user.domain.model.UserId;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId userId);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    void delete(User user);
}