package com.signly.contract.application;

import com.signly.common.exception.ForbiddenException;
import com.signly.contract.domain.model.Contract;
import com.signly.template.domain.model.ContractTemplate;
import org.springframework.stereotype.Service;

/**
 * 계약서 권한 검증 서비스
 * SRP: 권한 검증 책임만 담당
 */
@Service
public class ContractAuthorizationService {

    public void validateOwnership(
            String userId,
            Contract contract
    ) {
        if (!contract.getCreatorId().value().equals(userId)) {
            throw new ForbiddenException("해당 계약서에 대한 권한이 없습니다");
        }
    }

    public void validateAccess(
            String userId,
            Contract contract
    ) {
        if (!contract.getCreatorId().value().equals(userId) &&
                !contract.getFirstParty().email().equals(userId) &&
                !contract.getSecondParty().email().equals(userId)) {
            throw new ForbiddenException("해당 계약서에 접근할 권한이 없습니다");
        }
    }

    public void validateTemplateOwnership(
            String userId,
            ContractTemplate template
    ) {
        if (!template.getOwnerId().value().equals(userId)) {
            throw new ForbiddenException("해당 템플릿에 대한 권한이 없습니다");
        }
    }

    public void validateSigningAccess(
            String signerEmail,
            Contract contract
    ) {
        if (!contract.getFirstParty().email().equals(signerEmail) &&
                !contract.getSecondParty().email().equals(signerEmail)) {
            throw new ForbiddenException("해당 계약서에 접근할 권한이 없습니다");
        }
    }
}
