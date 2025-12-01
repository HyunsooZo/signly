package com.deally.contract.application;

import com.deally.common.exception.NotFoundException;
import com.deally.common.html.HtmlEntityNormalizer;
import com.deally.common.image.ImageResizer;
import com.deally.common.storage.FileStorageService;
import com.deally.contract.application.support.ContractHtmlSanitizer;
import com.deally.contract.domain.model.ContractId;
import com.deally.contract.domain.model.ContractPdfData;
import com.deally.contract.domain.model.GeneratedPdf;
import com.deally.contract.domain.model.Signature;
import com.deally.contract.domain.repository.ContractRepository;
import com.deally.contract.domain.repository.SignatureRepository;
import com.deally.contract.domain.service.PdfGenerator;
import com.deally.signature.application.FirstPartySignatureService;
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
    private final ImageResizer imageResizer;
    private final FirstPartySignatureService firstPartySignatureService;

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

        // 갑(사업주) 서명 이미지 조회 - FirstPartySignatureService 사용
        String firstPartySignatureImage = getFirstPartySignatureImage(contract.getCreatorId().value());

        // 을(근로자) 서명 이미지 조회 - SignatureRepository 사용
        String secondPartySignatureImage = getSecondPartySignatureImage(cId, contract.getSecondParty().email());

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
     * 갑(사업주) 서명 이미지 조회 - FirstPartySignatureService 사용
     * @param ownerId 사업주 ID
     * @return 서명 이미지 Data URL, 없으면 null
     */
    private String getFirstPartySignatureImage(String ownerId) {
        try {
            // FirstPartySignatureService를 통해 파일에서 원본 서명 로드
            String dataUrl = firstPartySignatureService.getSignatureDataUrl(ownerId);
            return imageResizer.resizeSignatureImage(dataUrl);
        } catch (Exception e) {
            logger.warn("갑(사업주) 서명을 찾을 수 없습니다: ownerId={}", ownerId);
            return null;
        }
    }

    /**
     * 을(근로자) 서명 이미지 조회 - SignatureRepository 사용
     * @param contractId 계약서 ID
     * @param signerEmail 서명자 이메일
     * @return 서명 이미지 Data URL, 없으면 null
     */
    private String getSecondPartySignatureImage(
            ContractId contractId,
            String signerEmail
    ) {
        String normalizedEmail = normalizeEmail(signerEmail);
        var signature = signatureRepository.findByContractIdAndSignerEmail(contractId, normalizedEmail);

        return signature
                .flatMap(this::buildSignatureDataUrl)
                .map(imageResizer::resizeSignatureImage)
                .orElse(null);
    }

    /**
     * 서명 이미지 조회 및 리사이징 (없으면 null 반환)
     * @deprecated 하위 호환성을 위해 유지, getFirstPartySignatureImage 또는 getSecondPartySignatureImage 사용 권장
     */
    @Deprecated
    private String getSignatureImage(
            ContractId contractId,
            String signerEmail
    ) {
        String normalizedEmail = normalizeEmail(signerEmail);
        var signature = signatureRepository.findByContractIdAndSignerEmail(contractId, normalizedEmail);

        return signature
                .flatMap(this::buildSignatureDataUrl)
                .map(imageResizer::resizeSignatureImage) // 리사이징 적용
                .orElse(null);
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

        // HtmlEntityNormalizer를 사용하여 플레이스홀더 정규화
        return HtmlEntityNormalizer.normalizePlaceholders(sanitized);
    }

    private String injectSignatureImage(
            String html,
            String dataUrl,
            String placeholder,
            int wrapperIndex
    ) {
        if (dataUrl == null || dataUrl.isBlank()) {
            // 서명이 없으면 placeholder만 제거
            return html.replace(placeholder, "");
        }

        String imageTag = createImageTag(dataUrl);

        // 1. placeholder가 있으면 교체 (새 계약서)
        if (html.contains(placeholder)) {
            logger.debug("플레이스홀더 발견, 서명 이미지로 교체: {}", placeholder);
            return html.replace(placeholder, imageTag);
        }

        // 2. 기존 이미지 태그가 있으면 교체 (기존 계약서 품질 개선)
        String existingImagePattern = "<img[^>]*class=\"signature-stamp-image-element\"[^>]*>";
        if (html.matches("(?s).*" + existingImagePattern + ".*")) {
            logger.debug("기존 서명 이미지 발견, 새 고품질 이미지로 교체");
            return html.replaceFirst(existingImagePattern, imageTag);
        }

        // 3. placeholder도 없고 이미지도 없으면 wrapper에 삽입 시도 (fallback)
        logger.debug("플레이스홀더와 기존 이미지 없음, wrapper에 삽입 시도");
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
