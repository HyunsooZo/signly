package com.signly.user.domain.repository;

import com.signly.user.domain.model.Email;
import com.signly.user.domain.model.User;
import com.signly.user.domain.model.UserId;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId userId);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    /**
     * 인증 토큰으로 사용자 조회
     *
     * @param token 인증 토큰
     * @return 사용자 (Optional)
     */
    Optional<User> findByVerificationToken(String token);

    void delete(User user);
}