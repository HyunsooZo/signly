package com.signly.contract.domain.model;

import java.time.LocalDateTime;

/**
 * PDF 문서를 나타내는 Value Object
 * DDD 원칙: 불변 객체로 설계, 비즈니스 로직 캡슐화
 */
public record GeneratedPdf(
        byte[] content,
        String fileName,
        long sizeInBytes,
        LocalDateTime generatedAt
) {

    public GeneratedPdf(
            byte[] content,
            String fileName,
            LocalDateTime generatedAt
    ) {
        this(content, fileName, content == null ? 0 : content.length, generatedAt);

        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("PDF 내용이 비어있습니다");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("파일명이 비어있습니다");
        }
        if (generatedAt == null) {
            throw new IllegalArgumentException("생성 시각이 비어있습니다");
        }
    }

    public static GeneratedPdf of(
            byte[] content,
            String fileName
    ) {
        return new GeneratedPdf(content, fileName, LocalDateTime.now());
    }

    public String getContentType() {
        return "application/pdf";
    }

    /**
     * 파일 크기가 제한을 초과하는지 확인 (비즈니스 규칙)
     *
     * @param maxSizeInMb 최대 크기 (MB)
     * @return 초과 여부
     */
    public boolean exceedsSize(int maxSizeInMb) {
        long maxBytes = maxSizeInMb * 1024L * 1024L;
        return this.sizeInBytes > maxBytes;
    }
}
