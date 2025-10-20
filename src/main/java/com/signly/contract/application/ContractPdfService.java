package com.signly.contract.application;

import com.signly.common.exception.NotFoundException;
import com.signly.contract.domain.model.*;
import com.signly.contract.domain.repository.ContractRepository;
import com.signly.contract.domain.service.PdfGenerator;
import com.signly.signature.domain.model.ContractSignature;
import com.signly.signature.domain.repository.SignatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 계약서 PDF 생성을 담당하는 Application Service
 * DDD 원칙: 도메인 객체와 Infrastructure를 오케스트레이션
 * SRP: PDF 생성 관련 비즈니스 로직만 담당
 */
@Service
@Transactional
public class ContractPdfService {

    private static final Logger logger = LoggerFactory.getLogger(ContractPdfService.class);

    private final ContractRepository contractRepository;
    private final SignatureRepository signatureRepository;
    private final PdfGenerator pdfGenerator;

    public ContractPdfService(
            ContractRepository contractRepository,
            SignatureRepository signatureRepository,
            PdfGenerator pdfGenerator
    ) {
        this.contractRepository = contractRepository;
        this.signatureRepository = signatureRepository;
        this.pdfGenerator = pdfGenerator;
    }

    /**
     * 계약서 ID로 PDF 생성
     *
     * @param contractId 계약서 ID
     * @return 생성된 PDF
     */
    public GeneratedPdf generateContractPdf(String contractId) {
        logger.info("계약서 PDF 생성 시작: contractId={}", contractId);

        ContractId cId = ContractId.of(contractId);
        Contract contract = contractRepository.findById(cId)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다: " + contractId));

        // 양측 서명 이미지 조회
        String firstPartySignatureImage = getSignatureImage(cId, contract.getFirstParty().getEmail());
        String secondPartySignatureImage = getSignatureImage(cId, contract.getSecondParty().getEmail());

        // PDF 데이터 구성
        ContractPdfData pdfData = ContractPdfData.builder()
                .contractId(contract.getId())
                .title(contract.getTitle())
                .htmlContent(contract.getContent().getValue())
                .firstPartySignatureImage(firstPartySignatureImage)
                .secondPartySignatureImage(secondPartySignatureImage)
                .presetType(contract.getPresetType())
                .build();

        if (!pdfData.hasBothSignatures()) {
            logger.warn("양측 서명이 모두 존재하지 않습니다: contractId={}", contractId);
        }

        // HTML에 서명 이미지를 삽입한 최종 HTML 생성
        String finalHtml = insertSignatureImages(pdfData);

        // PDF 생성
        GeneratedPdf pdf = pdfGenerator.generateFromHtml(finalHtml, pdfData.generateFileName());

        logger.info("계약서 PDF 생성 완료: contractId={}, fileName={}, size={}bytes",
                contractId, pdf.getFileName(), pdf.getSizeInBytes());

        return pdf;
    }

    /**
     * 서명 이미지 조회 (없으면 null 반환)
     */
    private String getSignatureImage(ContractId contractId, String signerEmail) {
        Optional<ContractSignature> signature = signatureRepository
                .findByContractIdAndSignerEmail(contractId, signerEmail);

        return signature.map(sig -> sig.signatureData().value()).orElse(null);
    }

    /**
     * HTML에 서명 이미지 삽입
     * SRP: 템플릿 치환 로직 분리
     */
    private String insertSignatureImages(ContractPdfData pdfData) {
        String html = pdfData.getHtmlContent();

        // 갑(첫번째 당사자) 서명 이미지 삽입
        if (pdfData.getFirstPartySignatureImage() != null) {
            String firstPartyImageTag = createImageTag(pdfData.getFirstPartySignatureImage());
            html = html.replace("[EMPLOYER_SIGNATURE_IMAGE]", firstPartyImageTag);
        } else {
            html = html.replace("[EMPLOYER_SIGNATURE_IMAGE]", "");
        }

        // 을(두번째 당사자) 서명 이미지 삽입
        if (pdfData.getSecondPartySignatureImage() != null) {
            String secondPartyImageTag = createImageTag(pdfData.getSecondPartySignatureImage());
            html = html.replace("[EMPLOYEE_SIGNATURE_IMAGE]", secondPartyImageTag);
        } else {
            html = html.replace("[EMPLOYEE_SIGNATURE_IMAGE]", "");
        }

        return html;
    }

    /**
     * Data URL을 img 태그로 변환
     */
    private String createImageTag(String dataUrl) {
        return String.format("<img src=\"%s\" class=\"signature-stamp-image-element\" alt=\"서명\"/>", dataUrl);
    }
}
