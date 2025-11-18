package com.signly.common.security;

import com.signly.user.domain.model.User;
import com.signly.user.domain.model.UserStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()));
    }

    @Override
    public String getPassword() {
        return user.getEncodedPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail().value();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }

    public String getUserId() {
        return user.getUserId().value();
    }

    public String getEmail() {
        return user.getEmail().value();
    }

    public String getName() {
        return user.getName();
    }

    public String getUserType() {
        return user.getUserType().name();
    }

    public String getCompanyName() {
        var company = user.getCompany();
        return company != null ? company.name() : null;
    }

    public String getBusinessPhone() {
        var company = user.getCompany();
        return company != null ? company.phone() : null;
    }

    public String getBusinessAddress() {
        var company = user.getCompany();
        return company != null ? company.address() : null;
    }
}