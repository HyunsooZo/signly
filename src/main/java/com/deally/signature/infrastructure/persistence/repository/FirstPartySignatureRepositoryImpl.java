package com.deally.signature.infrastructure.persistence.repository;

import com.deally.signature.domain.model.FirstPartySignature;
import com.deally.signature.domain.repository.FirstPartySignatureRepository;
import com.deally.signature.infrastructure.persistence.mapper.FirstPartySignatureEntityMapper;
import com.deally.user.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FirstPartySignatureRepositoryImpl implements FirstPartySignatureRepository {

    private final FirstPartySignatureJpaRepository jpaRepository;
    private final FirstPartySignatureEntityMapper mapper;

    @Override
    public FirstPartySignature save(FirstPartySignature signature) {
        var entity = mapper.toEntity(signature);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<FirstPartySignature> findByOwnerId(UserId ownerId) {
        return jpaRepository.findByOwnerId(ownerId.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByOwnerId(UserId ownerId) {
        return jpaRepository.existsByOwnerId(ownerId.value());
    }

    @Override
    public void deleteByOwnerId(UserId ownerId) {
        jpaRepository.deleteByOwnerId(ownerId.value());
    }
}
