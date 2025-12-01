package com.deally.signature.infrastructure.persistence.repository;

import com.deally.signature.infrastructure.persistence.entity.FirstPartySignatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FirstPartySignatureJpaRepository extends JpaRepository<FirstPartySignatureEntity, String> {

    Optional<FirstPartySignatureEntity> findByOwnerId(String ownerId);

    boolean existsByOwnerId(String ownerId);

    void deleteByOwnerId(String ownerId);
}
