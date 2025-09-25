package com.signly.contract.application.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class SignatureResponse {
    private final String signerEmail;
    private final String signerName;
    private final LocalDateTime signedAt;
    private final String ipAddress;

    public SignatureResponse(String signerEmail,
                             String signerName,
                             LocalDateTime signedAt,
                             String ipAddress) {
        this.signerEmail = signerEmail;
        this.signerName = signerName;
        this.signedAt = signedAt;
        this.ipAddress = ipAddress;
    }

    public String getSignerEmail() {
        return signerEmail;
    }

    public String signerEmail() {
        return signerEmail;
    }

    public String getSignerName() {
        return signerName;
    }

    public String signerName() {
        return signerName;
    }

    public Date getSignedAt() {
        if (signedAt == null) {
            return null;
        }
        return Date.from(signedAt.atZone(ZoneId.systemDefault()).toInstant());
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String ipAddress() {
        return ipAddress;
    }
}
