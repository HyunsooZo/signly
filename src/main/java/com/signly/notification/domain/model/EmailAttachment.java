package com.signly.notification.domain.model;

import java.util.Objects;

/**
 * 이메일 첨부파일을 나타내는 Value Object
 * DDD 원칙: 불변 객체로 설계
 * SRP: 첨부파일 데이터만 표현
 */
public class EmailAttachment {
    private final String fileName;
    private final byte[] content;
    private final String contentType;

    private EmailAttachment(String fileName, byte[] content, String contentType) {
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
        this.content = content.clone(); // 방어적 복사
        this.contentType = contentType;
    }

    public static EmailAttachment of(String fileName, byte[] content, String contentType) {
        return new EmailAttachment(fileName, content, contentType);
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content.clone(); // 방어적 복사
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeInBytes() {
        return content.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailAttachment that = (EmailAttachment) o;
        return Objects.equals(fileName, that.fileName) &&
                Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, contentType);
    }

    @Override
    public String toString() {
        return "EmailAttachment{" +
                "fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", sizeInBytes=" + getSizeInBytes() +
                '}';
    }
}
