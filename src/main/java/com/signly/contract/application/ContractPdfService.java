package com.signly.contract.application;

import com.signly.common.exception.NotFoundException;
import com.signly.common.storage.FileStorageService;
import com.signly.contract.application.support.ContractHtmlSanitizer;
import com.signly.contract.domain.model.*;
import com.signly.contract.domain.repository.ContractRepository;
import com.signly.contract.domain.service.PdfGenerator;
import com.signly.contract.domain.model.Signature;
import com.signly.contract.domain.repository.SignatureRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;

/**
 * 계약서 PDF 생성을 담당하는 Application Service
 * DDD 원칙: 도메인 객체와 Infrastructure를 오케스트레이션
 * SRP: PDF 생성 관련 비즈니스 로직만 담당
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ContractPdfService {

    private static final Logger logger = LoggerFactory.getLogger(ContractPdfService.class);
    private final ContractRepository contractRepository;
    private final SignatureRepository signatureRepository;
    private final PdfGenerator pdfGenerator;
    private final FileStorageService fileStorageService;

    /**
     * 계약서 ID로 PDF 생성
     *
     * @param contractId 계약서 ID
     * @return 생성된 PDF
     */
    public GeneratedPdf generateContractPdf(String contractId) {
        logger.info("계약서 PDF 생성 시작: contractId={}", contractId);

        var cId = ContractId.of(contractId);
        var contract = contractRepository.findById(cId)
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다: " + contractId));

        // 양측 서명 이미지 조회
        String firstPartySignatureImage = getSignatureImage(cId, contract.getFirstParty().email());
        String secondPartySignatureImage = getSignatureImage(cId, contract.getSecondParty().email());

        // PDF 데이터 구성
        var pdfData = ContractPdfData.builder()
                .contractId(contract.getId())
                .title(contract.getTitle())
                .htmlContent(contract.getContent().content())
                .firstPartySignatureImage(firstPartySignatureImage)
                .secondPartySignatureImage(secondPartySignatureImage)
                .presetType(contract.getPresetType())
                .build();

        if (!pdfData.hasBothSignatures()) {
            logger.warn("양측 서명이 모두 존재하지 않습니다: contractId={}", contractId);
        }

        // HTML에 서명 이미지를 삽입한 최종 HTML 생성
        String finalHtml = insertSignatureImages(pdfData);

        GeneratedPdf pdf;
        try {
            pdf = pdfGenerator.generateFromHtml(finalHtml, pdfData.generateFileName());
        } catch (Exception ex) {
            dumpPdfHtmlDebug(contractId, finalHtml);
            throw ex;
        }

        logger.info("계약서 PDF 생성 완료: contractId={}, fileName={}, size={}bytes",
                contractId, pdf.fileName(), pdf.sizeInBytes());

        return pdf;
    }

    /**
     * 서명 이미지 조회 (없으면 null 반환)
     */
    private String getSignatureImage(
            ContractId contractId,
            String signerEmail
    ) {
        String normalizedEmail = normalizeEmail(signerEmail);
        var signature = signatureRepository.findByContractIdAndSignerEmail(contractId, normalizedEmail);

        return signature.flatMap(this::buildSignatureDataUrl).orElse(null);
    }

    /**
     * HTML에 서명 이미지 삽입
     * SRP: 템플릿 치환 로직 분리
     */
    private String insertSignatureImages(ContractPdfData pdfData) {
        String html = normalizeSignaturePlaceholders(pdfData.htmlContent());
        if (html == null) {
            return "";
        }

        html = injectSignatureImage(
                html,
                pdfData.firstPartySignatureImage(),
                "[EMPLOYER_SIGNATURE_IMAGE]",
                1
        );

        html = injectSignatureImage(
                html,
                pdfData.secondPartySignatureImage(),
                "[EMPLOYEE_SIGNATURE_IMAGE]",
                2
        );

        return html;
    }

    private String normalizeSignaturePlaceholders(String html) {
        String sanitized = ContractHtmlSanitizer.sanitize(html);

        if (sanitized.isBlank()) {
            return sanitized;
        }

        return sanitized
                .replace("&#91;EMPLOYER_SIGNATURE_IMAGE&#93;", "[EMPLOYER_SIGNATURE_IMAGE]")
                .replace("&#91;EMPLOYEE_SIGNATURE_IMAGE&#93;", "[EMPLOYEE_SIGNATURE_IMAGE]")
                .replace("&amp;#91;EMPLOYER_SIGNATURE_IMAGE&amp;#93;", "[EMPLOYER_SIGNATURE_IMAGE]")
                .replace("&amp;#91;EMPLOYEE_SIGNATURE_IMAGE&amp;#93;", "[EMPLOYEE_SIGNATURE_IMAGE]")
                .replace("&lbrack;EMPLOYER_SIGNATURE_IMAGE&rbrack;", "[EMPLOYER_SIGNATURE_IMAGE]")
                .replace("&lbrack;EMPLOYEE_SIGNATURE_IMAGE&rbrack;", "[EMPLOYEE_SIGNATURE_IMAGE]");
    }

    private String injectSignatureImage(
            String html,
            String dataUrl,
            String placeholder,
            int wrapperIndex
    ) {
        if (dataUrl == null || dataUrl.isBlank()) {
            return html.replace(placeholder, "");
        }

        String imageTag = createImageTag(dataUrl);

        if (html.contains(placeholder)) {
            return html.replace(placeholder, imageTag);
        }

        return insertIntoWrapper(html, imageTag, wrapperIndex);
    }

    private String insertIntoWrapper(
            String html,
            String imageTag,
            int occurrence
    ) {
        int searchIndex = 0;

        for (int i = 0; i < occurrence; i++) {
            int marker = html.indexOf("signature-stamp-wrapper", searchIndex);
            if (marker == -1) {
                return html;
            }

            int tagStart = html.lastIndexOf('<', marker);
            int openingEnd = html.indexOf('>', marker);
            if (tagStart == -1 || openingEnd == -1) {
                return html;
            }

            int wrapperEnd = findMatchingSpanEnd(html, openingEnd + 1);
            if (wrapperEnd == -1) {
                return html;
            }

            if (i == occurrence - 1) {
                String inner = html.substring(openingEnd + 1, wrapperEnd - "</span>".length());
                String innerText = inner
                        .replace("&nbsp;", "")
                        .replace("&#160;", "")
                        .replaceAll("<[^>]+>", "")
                        .trim();

                if (!innerText.isEmpty()) {
                    return html;
                }

                return html.substring(0, openingEnd + 1) + imageTag + "</span>" + html.substring(wrapperEnd);
            }

            searchIndex = wrapperEnd;
        }

        return html;
    }

    private int findMatchingSpanEnd(
            String html,
            int searchFrom
    ) {
        int depth = 1;
        int index = searchFrom;

        while (index < html.length()) {
            int nextOpen = html.indexOf("<span", index);
            int nextClose = html.indexOf("</span>", index);

            if (nextClose == -1) {
                return -1;
            }

            if (nextOpen != -1 && nextOpen < nextClose) {
                depth++;
                index = nextOpen + 5;
            } else {
                depth--;
                index = nextClose + "</span>".length();
                if (depth == 0) {
                    return index;
                }
            }
        }

        return -1;
    }

    /**
     * Data URL을 img 태그로 변환
     */
    private String createImageTag(String dataUrl) {
        return String.format("<img src=\"%s\" class=\"signature-stamp-image-element\" alt=\"서명\"/>", dataUrl);
    }

    private Optional<String> buildSignatureDataUrl(Signature signature) {
        String signaturePath = signature.signaturePath();
        String originalDataUrl = signature.signatureData();

        if (signaturePath != null && !signaturePath.isBlank()) {
            try {
                byte[] imageBytes = fileStorageService.loadFile(signaturePath);
                String contentType = extractContentType(originalDataUrl);
                String base64 = Base64.getEncoder().encodeToString(imageBytes);
                return Optional.of("data:" + contentType + ";base64," + base64);
            } catch (Exception e) {
                logger.warn("서명 이미지 파일을 로드할 수 없어 DB 데이터를 사용합니다: path={}", signaturePath, e);
            }
        }

        return Optional.ofNullable(originalDataUrl);
    }

    private String extractContentType(String dataUrl) {
        if (dataUrl == null || !dataUrl.startsWith("data:")) {
            return "image/png";
        }
        int semicolonIndex = dataUrl.indexOf(';');
        if (semicolonIndex > 5) {
            return dataUrl.substring(5, semicolonIndex);
        }
        return "image/png";
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private void dumpPdfHtmlDebug(
            String contractId,
            String html
    ) {
        try {
            Path debugDir = Path.of("logs", "pdf-debug");
            Files.createDirectories(debugDir);
            Path file = debugDir.resolve("contract-" + contractId + ".html");
            Files.writeString(file, html);
            logger.error("PDF 생성 실패: 디버그 HTML 저장됨 -> {}", file.toAbsolutePath());
        } catch (Exception dumpEx) {
            logger.error("PDF 생성 실패 HTML 저장 중 추가 오류 발생", dumpEx);
        }
    }
}
