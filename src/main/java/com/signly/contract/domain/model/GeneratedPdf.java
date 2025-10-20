package com.signly.contract.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * PDF 문서를 나타내는 Value Object
 * DDD 원칙: 불변 객체로 설계, 비즈니스 로직 캡슐화
 */
public class GeneratedPdf {
    private final byte[] content;
    private final String fileName;
    private final long sizeInBytes;
    private final LocalDateTime generatedAt;

    private GeneratedPdf(byte[] content, String fileName, LocalDateTime generatedAt) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("PDF 내용이 비어있습니다");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("파일명이 비어있습니다");
        }
        if (generatedAt == null) {
            throw new IllegalArgumentException("생성 시각이 비어있습니다");
        }

        this.content = content.clone(); // 방어적 복사
        this.fileName = fileName;
        this.sizeInBytes = content.length;
        this.generatedAt = generatedAt;
    }

    public static GeneratedPdf of(byte[] content, String fileName) {
        return new GeneratedPdf(content, fileName, LocalDateTime.now());
    }

    public byte[] getContent() {
        return content.clone(); // 방어적 복사
    }

    public String getFileName() {
        return fileName;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public String getContentType() {
        return "application/pdf";
    }

    /**
     * 파일 크기가 제한을 초과하는지 확인 (비즈니스 규칙)
     * @param maxSizeInMb 최대 크기 (MB)
     * @return 초과 여부
     */
    public boolean exceedsSize(int maxSizeInMb) {
        long maxBytes = maxSizeInMb * 1024L * 1024L;
        return this.sizeInBytes > maxBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneratedPdf that = (GeneratedPdf) o;
        return sizeInBytes == that.sizeInBytes &&
                Objects.equals(fileName, that.fileName) &&
                Objects.equals(generatedAt, that.generatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, sizeInBytes, generatedAt);
    }

    @Override
    public String toString() {
        return "GeneratedPdf{" +
                "fileName='" + fileName + '\'' +
                ", sizeInBytes=" + sizeInBytes +
                ", generatedAt=" + generatedAt +
                '}';
    }
}
