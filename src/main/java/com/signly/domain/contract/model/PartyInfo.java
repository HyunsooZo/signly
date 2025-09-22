package com.signly.domain.contract.model;

import com.signly.common.exception.ValidationException;

import java.util.Objects;
import java.util.regex.Pattern;

public class PartyInfo {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private final String name;
    private final String email;
    private final String organizationName;

    private PartyInfo(String name, String email, String organizationName) {
        this.name = name;
        this.email = email;
        this.organizationName = organizationName;
    }

    public static PartyInfo of(String name, String email, String organizationName) {
        validateName(name);
        validateEmail(email);
        validateOrganizationName(organizationName);

        return new PartyInfo(name.trim(), email.trim().toLowerCase(),
                           organizationName != null ? organizationName.trim() : null);
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

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public boolean hasOrganization() {
        return organizationName != null && !organizationName.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartyInfo partyInfo = (PartyInfo) o;
        return Objects.equals(name, partyInfo.name) &&
               Objects.equals(email, partyInfo.email) &&
               Objects.equals(organizationName, partyInfo.organizationName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, organizationName);
    }

    @Override
    public String toString() {
        return "PartyInfo{" +
               "name='" + name + '\'' +
               ", email='" + email + '\'' +
               ", organizationName='" + organizationName + '\'' +
               '}';
    }
}