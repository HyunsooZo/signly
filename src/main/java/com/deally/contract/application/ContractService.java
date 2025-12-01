package com.deally.contract.application;

import com.deally.contract.application.dto.ContractResponse;
import com.deally.contract.application.dto.CreateContractCommand;
import com.deally.contract.application.dto.UpdateContractCommand;
import com.deally.contract.application.mapper.ContractDtoMapper;
import com.deally.contract.domain.model.Contract;
import com.deally.contract.domain.model.ContractStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 계약서 서비스 Facade
 * 기존 API를 유지하면서 내부적으로 책임별로 분리된 서비스들에 위임
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ContractService {

    private final ContractCreationService creationService;
    private final ContractSigningCoordinator signingCoordinator;
    private final ContractQueryService queryService;
    private final ContractAuthorizationService authorizationService;
    private final ContractDtoMapper contractDtoMapper;

    public ContractResponse createContract(
            String userId,
            CreateContractCommand command
    ) {
        var contract = creationService.createContract(userId, command);
        return contractDtoMapper.toResponse(contract);
    }

    public ContractResponse updateContract(
            String userId,
            String contractId,
            UpdateContractCommand command
    ) {
        var contract = creationService.updateContract(userId, contractId, command);
        return contractDtoMapper.toResponse(contract);
    }

    public void deleteContract(
            String userId,
            String contractId
    ) {
        creationService.deleteContract(userId, contractId);
    }

    public void sendForSigning(
            String userId,
            String contractId
    ) {
        signingCoordinator.sendForSigning(userId, contractId);
    }

    public void resendSigningEmail(
            String userId,
            String contractId
    ) {
        signingCoordinator.resendSigningEmail(userId, contractId);
    }



    public ContractResponse processSignature(
            String token,
            String signerEmail,
            String signerName,
            String signatureData,
            String ipAddress
    ) {
        Contract contract = signingCoordinator.processSignature(token, signerEmail, signerName, signatureData, ipAddress);
        return contractDtoMapper.toResponse(contract);
    }

    public void cancelContract(
            String userId,
            String contractId
    ) {
        signingCoordinator.cancelContract(userId, contractId);
    }

    public void expireContracts() {
        signingCoordinator.expireContracts();
    }

    @Transactional(readOnly = true)
    public ContractResponse getContract(
            String userId,
            String contractId
    ) {
        Contract contract = queryService.findById(contractId);
        authorizationService.validateAccess(userId, contract);
        return contractDtoMapper.toResponse(contract);
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractForSigning(
            String signerEmail,
            String contractId
    ) {
        Contract contract = queryService.findById(contractId);
        authorizationService.validateSigningAccess(signerEmail, contract);
        return contractDtoMapper.toResponse(contract);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByCreator(
            String userId,
            Pageable pageable
    ) {
        return queryService.getContractsByCreator(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByCreatorAndStatus(
            String userId,
            ContractStatus status,
            Pageable pageable
    ) {
        return queryService.getContractsByCreatorAndStatus(userId, status, pageable);
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractByToken(String token) {
        return queryService.getContractByToken(token);
    }
}
