package com.signly.notification.domain.model;

/**
 * 이메일 첨부파일을 나타내는 Value Object
 * DDD 원칙: 불변 객체로 설계
 * SRP: 첨부파일 데이터만 표현
 */
public record EmailAttachment(
        String fileName,
        byte[] content,
        String contentType
) {

    public EmailAttachment(
            String fileName,
            byte[] content,
            String contentType
    ) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("파일명이 비어있습니다");
        }
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("첨부파일 내용이 비어있습니다");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content Type이 비어있습니다");
        }
        this.fileName = fileName;
        this.content = content.clone();
        this.contentType = contentType;
    }

    public static EmailAttachment of(
            String fileName,
            byte[] content,
            String contentType
    ) {
        return new EmailAttachment(fileName, content, contentType);
    }

    public byte[] content() {
        return content.clone(); // 방어적 복사
    }

    public long sizeInBytes() {
        return content.length;
    }

}
