package com.signly.user.domain.model;

import com.signly.common.exception.ValidationException;

/**
 * 사업장 정보 Value Object
 */
public record Company(String name, String phone, String address) {

    public static Company of(
            String name,
            String phone,
            String address
    ) {
        validateCompanyName(name);
        validatePhone(phone);
        validateAddress(address);

        return new Company(name, phone, address);
    }

    public static Company empty() {
        return new Company(null, null, null);
    }

    private static void validateCompanyName(String name) {
        if (name != null && name.trim().length() > 200) {
            throw new ValidationException("사업장명은 200자를 초과할 수 없습니다");
        }
    }

    private static void validatePhone(String phone) {
        if (phone != null && phone.trim().length() > 20) {
            throw new ValidationException("사업장 전화번호는 20자를 초과할 수 없습니다");
        }
    }

    private static void validateAddress(String address) {
        if (address != null && address.trim().length() > 500) {
            throw new ValidationException("사업장 주소는 500자를 초과할 수 없습니다");
        }
    }

    public boolean isEmpty() {
        return name == null && phone == null && address == null;
    }

}
