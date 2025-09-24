package com.signly.signature.domain.model;

import com.signly.common.exception.ValidationException;

import java.time.LocalDateTime;

public class SignerInfo {
    private final String signerEmail;
    private final String signerName;
    private final String ipAddress;
    private final String deviceInfo;
    private final LocalDateTime signedAt;

    private SignerInfo(String signerEmail, String signerName, String ipAddress,
                      String deviceInfo, LocalDateTime signedAt) {
        this.signerEmail = signerEmail;
        this.signerName = signerName;
        this.ipAddress = ipAddress;
        this.deviceInfo = deviceInfo;
        this.signedAt = signedAt;
    }

    public static SignerInfo create(String signerEmail, String signerName,
                                   String ipAddress, String deviceInfo) {
        if (signerEmail == null || signerEmail.trim().isEmpty()) {
            throw new ValidationException("서명자 이메일은 필수입니다");
        }

        if (signerName == null || signerName.trim().isEmpty()) {
            throw new ValidationException("서명자 이름은 필수입니다");
        }

        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            throw new ValidationException("IP 주소는 필수입니다");
        }

        return new SignerInfo(
                signerEmail.trim(),
                signerName.trim(),
                ipAddress.trim(),
                deviceInfo != null ? deviceInfo.trim() : "",
                LocalDateTime.now()
        );
    }

    public String signerEmail() {
        return signerEmail;
    }

    public String signerName() {
        return signerName;
    }

    public String ipAddress() {
        return ipAddress;
    }

    public String deviceInfo() {
        return deviceInfo;
    }

    public LocalDateTime signedAt() {
        return signedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignerInfo that = (SignerInfo) o;
        return signerEmail.equals(that.signerEmail) &&
               signerName.equals(that.signerName) &&
               signedAt.equals(that.signedAt);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(signerEmail, signerName, signedAt);
    }

    @Override
    public String toString() {
        return "SignerInfo{" +
                "signerEmail='" + signerEmail + '\'' +
                ", signerName='" + signerName + '\'' +
                ", signedAt=" + signedAt +
                '}';
    }
}