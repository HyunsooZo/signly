package com.signly.domain.user.repository;

import com.signly.domain.user.model.Email;
import com.signly.domain.user.model.User;
import com.signly.domain.user.model.UserId;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId userId);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    void delete(User user);
}