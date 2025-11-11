package com.signly.signature.infrastructure.persistence.repository;

import com.signly.signature.domain.model.FirstPartySignature;
import com.signly.signature.domain.repository.FirstPartySignatureRepository;
import com.signly.signature.infrastructure.persistence.entity.FirstPartySignatureEntity;
import com.signly.signature.infrastructure.persistence.mapper.FirstPartySignatureEntityMapper;
import com.signly.user.domain.model.UserId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class FirstPartySignatureRepositoryImpl implements FirstPartySignatureRepository {

    private final FirstPartySignatureJpaRepository jpaRepository;
    private final FirstPartySignatureEntityMapper mapper;

    public FirstPartySignatureRepositoryImpl(
            FirstPartySignatureJpaRepository jpaRepository,
            FirstPartySignatureEntityMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public FirstPartySignature save(FirstPartySignature signature) {
        FirstPartySignatureEntity entity = mapper.toEntity(signature);
        FirstPartySignatureEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<FirstPartySignature> findByOwnerId(UserId ownerId) {
        return jpaRepository.findByOwnerId(ownerId.value())
                .map(mapper::toDomain);
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
