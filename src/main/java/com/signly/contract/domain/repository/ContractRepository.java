package com.signly.contract.domain.repository;

import com.signly.contract.domain.model.Contract;
import com.signly.contract.domain.model.ContractId;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.contract.domain.model.SignToken;
import com.signly.template.domain.model.TemplateId;
import com.signly.user.domain.model.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ContractRepository {
    Contract save(Contract contract);

    Optional<Contract> findById(ContractId contractId);

    Optional<Contract> findBySignToken(SignToken signToken);

    void delete(Contract contract);

    Page<Contract> findByCreatorId(
            UserId creatorId,
            Pageable pageable
    );

    Page<Contract> findByCreatorIdAndStatus(
            UserId creatorId,
            ContractStatus status,
            Pageable pageable
    );

    Page<Contract> findByPartyEmail(
            String email,
            Pageable pageable
    );

    Page<Contract> findByPartyEmailAndStatus(
            String email,
            ContractStatus status,
            Pageable pageable
    );

    List<Contract> findByTemplateId(TemplateId templateId);

    List<Contract> findExpiredContracts(LocalDateTime currentTime);

    List<Contract> findByStatusAndExpiresAtBefore(
            ContractStatus status,
            LocalDateTime dateTime
    );

    boolean existsByCreatorIdAndTitle(
            UserId creatorId,
            String title
    );

    long countByCreatorId(UserId creatorId);

    long countByCreatorIdAndStatus(
            UserId creatorId,
            ContractStatus status
    );

    long countByTemplateId(TemplateId templateId);
}