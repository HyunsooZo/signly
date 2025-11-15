package com.signly.contract.domain.model;

import com.signly.common.exception.ValidationException;

import java.time.LocalDateTime;

public record Signature(
        String signerEmail,
        String signerName,
        LocalDateTime signedAt,
        String signatureData,
        String ipAddress,
        String deviceInfo,
        String signaturePath
) {

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
}