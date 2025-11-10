package com.signly.contract.domain.model;

import com.signly.common.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;

public class Signature {
    private final String signerEmail;
    private final String signerName;
    private final LocalDateTime signedAt;
    private final String signatureData;
    private final String ipAddress;
    private final String deviceInfo;
    private final String signaturePath;

    private Signature(
            String signerEmail,
            String signerName,
            LocalDateTime signedAt,
            String signatureData,
            String ipAddress,
            String deviceInfo,
            String signaturePath
    ) {
        this.signerEmail = signerEmail;
        this.signerName = signerName;
        this.signedAt = signedAt;
        this.signatureData = signatureData;
        this.ipAddress = ipAddress;
        this.deviceInfo = deviceInfo;
        this.signaturePath = signaturePath;
    }

    public static Signature create(
            String signerEmail,
            String signerName,
            String signatureData,
            String ipAddress
    ) {
        return create(signerEmail, signerName, signatureData, ipAddress, null, null);
    }

    public static Signature create(
            String signerEmail,
            String signerName,
            String signatureData,
            String ipAddress,
            String deviceInfo,
            String signaturePath
    ) {
        validateSignerEmail(signerEmail);
        validateSignerName(signerName);
        validateSignatureData(signatureData);
        validateIpAddress(ipAddress);

        return new Signature(signerEmail.trim().toLowerCase(), signerName.trim(),
                LocalDateTime.now(), signatureData, ipAddress, deviceInfo, signaturePath);
    }

    private static void validateSignerEmail(String signerEmail) {
        if (signerEmail == null || signerEmail.trim().isEmpty()) {
            throw new ValidationException("서명자 이메일은 필수입니다");
        }
    }

    private static void validateSignerName(String signerName) {
        if (signerName == null || signerName.trim().isEmpty()) {
            throw new ValidationException("서명자 이름은 필수입니다");
        }
    }

    private static void validateSignatureData(String signatureData) {
        if (signatureData == null || signatureData.trim().isEmpty()) {
            throw new ValidationException("서명 데이터는 필수입니다");
        }
    }

    private static void validateIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            throw new ValidationException("IP 주소는 필수입니다");
        }
    }

    public String getSignerEmail() {
        return signerEmail;
    }

    public String getSignerName() {
        return signerName;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public String getSignatureData() {
        return signatureData;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public String getSignaturePath() {
        return signaturePath;
    }

    public boolean isSignedBy(String email) {
        return signerEmail.equals(email.trim().toLowerCase());
    }

    public boolean validate() {
        return !signatureData.isEmpty() &&
                signerEmail != null &&
                signerName != null &&
                ipAddress != null;
    }

    public boolean verifyIntegrity() {
        return validate() && signatureData != null && !signatureData.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signature signature = (Signature) o;
        return Objects.equals(signerEmail, signature.signerEmail) &&
                Objects.equals(signerName, signature.signerName) &&
                Objects.equals(signedAt, signature.signedAt) &&
                Objects.equals(signatureData, signature.signatureData) &&
                Objects.equals(ipAddress, signature.ipAddress) &&
                Objects.equals(deviceInfo, signature.deviceInfo) &&
                Objects.equals(signaturePath, signature.signaturePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signerEmail, signerName, signedAt, signatureData, ipAddress, deviceInfo, signaturePath);
    }

    @Override
    public String toString() {
        return "Signature{" +
                "signerEmail='" + signerEmail + '\'' +
                ", signerName='" + signerName + '\'' +
                ", signedAt=" + signedAt +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}