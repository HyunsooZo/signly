package com.deally.signature.domain.repository;

import com.deally.signature.domain.model.FirstPartySignature;
import com.deally.user.domain.model.UserId;

import java.util.Optional;

public interface FirstPartySignatureRepository {

    FirstPartySignature save(FirstPartySignature signature);

    Optional<FirstPartySignature> findByOwnerId(UserId ownerId);

    boolean existsByOwnerId(UserId ownerId);

    void deleteByOwnerId(UserId ownerId);
}
