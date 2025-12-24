package com.signly.common.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserPrincipal extends UserDetails {

    String getUserId();

    String getEmail();

    String getName();

    String getUserType();

    String getCompanyName();

    String getBusinessPhone();

    String getBusinessAddress();
}
