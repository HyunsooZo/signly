package com.deally.contract.domain.service;

import com.deally.common.exception.ValidationException;
import com.deally.contract.domain.model.Contract;
import com.deally.contract.domain.model.Signature;
import org.springframework.stereotype.Service;

/**
 * 계약서 서명 처리 도메인 서비스
 * SRP: 계약서 서명 관련 복잡한 비즈니스 로직을 담당
 */
@Service
public class ContractSigningService {

    /**
     * 계약서 서명 처리
     *
     * @param contract 서명할 계약서
     * @param request  서명 요청 정보
     * @return 생성된 서명 정보
     */
    public SigningResult processSigning(
            Contract contract,
            SigningRequest request
    ) {
        validateSigningRequest(contract, request);

        var signature = createSignature(request);
        contract.addSignature(signature);

        boolean isFullySigned = contract.isFullySigned();
        if (isFullySigned) {
            contract.markAsFullySigned();
        }

        return new SigningResult(signature, isFullySigned);
    }

    /**
     * 외부에서 서명 데이터를 저장한 후 상태만 업데이트할 때 사용
     *
     * @param contract              계약서
     * @param signerEmail           서명자 이메일
     * @param allSignaturesComplete 모든 서명이 완료되었는지 여부
     */
    public void markSignedBy(
            Contract contract,
            String signerEmail,
            boolean allSignaturesComplete
    ) {
        validateSigningEligibility(contract, signerEmail);

        if (allSignaturesComplete) {
            contract.markAsFullySigned();
        }
    }

    /**
     * 서명 요청 유효성 검증
     */
    private void validateSigningRequest(
            Contract contract,
            SigningRequest request
    ) {
        validateSigningEligibility(contract, request.signerEmail());
        validateDuplicateSigning(contract, request.signerEmail());
    }

    /**
     * 서명 자격 검증
     */
    private void validateSigningEligibility(
            Contract contract,
            String signerEmail
    ) {
        if (!contract.getStatus().canSign()) {
            throw new ValidationException("서명 대기 상태에서만 서명할 수 있습니다");
        }

        if (contract.isExpired()) {
            contract.expire();
            throw new ValidationException("만료된 계약서에는 서명할 수 없습니다");
        }

        if (!isValidSigner(contract, signerEmail)) {
            throw new ValidationException("해당 계약서에 서명할 권한이 없습니다");
        }
    }

    /**
     * 중복 서명 검증
     */
    private void validateDuplicateSigning(
            Contract contract,
            String signerEmail
    ) {
        if (hasSignedBy(contract, signerEmail)) {
            throw new ValidationException("이미 서명한 계약서입니다");
        }
    }

    /**
     * 서명자 권한 확인
     */
    private boolean isValidSigner(
            Contract contract,
            String email
    ) {
        return contract.getFirstParty().email().equals(email.trim().toLowerCase()) ||
                contract.getSecondParty().email().equals(email.trim().toLowerCase());
    }

    /**
     * 서명 완료 여부 확인
     */
    private boolean hasSignedBy(
            Contract contract,
            String email
    ) {
        return contract.getSignatures().stream()
                .anyMatch(signature -> signature.isSignedBy(email));
    }

    /**
     * 서명 객체 생성
     */
    private Signature createSignature(SigningRequest request) {
        return Signature.create(
                request.signerEmail(),
                request.signerName(),
                request.signatureData(),
                request.ipAddress()
        );
    }

    /**
     * 서명 요청 정보
     */
    public record SigningRequest(
            String signerEmail,
            String signerName,
            String signatureData,
            String ipAddress
    ) {}

    /**
     * 서명 처리 결과
     */
    public record SigningResult(
            Signature signature,
            boolean isFullySigned
    ) {}
}