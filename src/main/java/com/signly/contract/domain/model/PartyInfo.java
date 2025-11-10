package com.signly.contract.domain.model;

import com.signly.common.exception.ValidationException;

import java.util.regex.Pattern;

public record PartyInfo(String name, String email, String organizationName) {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    public PartyInfo {
        validateName(name);
        validateEmail(email);
        validateOrganizationName(organizationName);
        
        // Normalize values
        name = name.trim();
        email = email.trim().toLowerCase();
        organizationName = organizationName != null ? organizationName.trim() : null;
    }
    
    public static PartyInfo of(String name, String email, String organizationName) {
        return new PartyInfo(name, email, organizationName);
    }
    
    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("당사자 이름은 필수입니다");
        }
        if (name.length() > 100) {
            throw new ValidationException("당사자 이름은 100자를 초과할 수 없습니다");
        }
    }
    
    private static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("당사자 이메일은 필수입니다");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("올바른 이메일 형식이 아닙니다");
        }
    }
    
    private static void validateOrganizationName(String organizationName) {
        if (organizationName != null && organizationName.length() > 200) {
            throw new ValidationException("조직명은 200자를 초과할 수 없습니다");
        }
    }
    
    public boolean hasOrganization() {
        return organizationName != null && !organizationName.trim().isEmpty();
    }
}