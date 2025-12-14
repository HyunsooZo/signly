package com.signly.user.infrastructure.persistence.repository;

import com.signly.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {

    // 이메일 해시로 사용자 찾기 (주 검색 방법)
    Optional<UserEntity> findByEmailHash(String emailHash);

    // 중복 가입 체크 (해시 기반)
    boolean existsByEmailHash(String emailHash);

    // 기존 이메일 기반 메서드 (하위 호환성 유지, 권장하지 않음)
    @Deprecated
    Optional<UserEntity> findByEmail(String email);

    @Deprecated
    boolean existsByEmail(String email);

    Optional<UserEntity> findByVerificationToken(String verificationToken);
}