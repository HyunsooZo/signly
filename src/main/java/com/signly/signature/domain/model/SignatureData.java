package com.signly.signature.domain.model;

import com.signly.common.exception.ValidationException;

public class SignatureData {
    private final String value;

    private SignatureData(String value) {
        this.value = value;
    }

    public static SignatureData of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("서명 데이터는 필수입니다");
        }

        if (!isValidBase64(value)) {
            throw new ValidationException("서명 데이터는 Base64 형식이어야 합니다");
        }

        return new SignatureData(value);
    }

    private static boolean isValidBase64(String data) {
        try {
            if (!data.startsWith("data:image/")) {
                return false;
            }

            String base64Part = data.substring(data.indexOf(",") + 1);
            java.util.Base64.getDecoder().decode(base64Part);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String value() {
        return value;
    }

    public boolean isEmpty() {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignatureData that = (SignatureData) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "SignatureData{" +
                "length=" + (value != null ? value.length() : 0) +
                '}';
    }
}