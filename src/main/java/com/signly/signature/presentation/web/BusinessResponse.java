package com.signly.signature.presentation.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessResponse {
    private String businessName;
    private String businessPhone;
    private String businessAddress;

    public static Object of(
            String companyName,
            String businessPhone,
            String businessAddress
    ) {
        return new BusinessResponse(companyName, businessPhone, businessAddress);
    }
}
