package com.signly.common.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDTO implements UserPrincipal, Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String email;
    private String encodedPassword;
    private String name;
    private String companyName;
    private String businessPhone;
    private String businessAddress;
    private String userType;
    private String status;

    private List<String> roles;

    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return encodedPassword;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public static UserDetailsDTO from(SecurityUser securityUser) {
        UserDetailsDTO dto = new UserDetailsDTO();
        dto.setUserId(securityUser.getUserId());
        dto.setEmail(securityUser.getEmail());
        dto.setEncodedPassword(securityUser.getPassword());
        dto.setName(securityUser.getName());
        dto.setCompanyName(securityUser.getCompanyName());
        dto.setBusinessPhone(securityUser.getBusinessPhone());
        dto.setBusinessAddress(securityUser.getBusinessAddress());
        dto.setUserType(securityUser.getUserType());
        dto.setStatus(securityUser.getUser().getStatus().name());

        dto.setRoles(
                securityUser.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );

        dto.setAccountNonExpired(securityUser.isAccountNonExpired());
        dto.setAccountNonLocked(securityUser.isAccountNonLocked());
        dto.setCredentialsNonExpired(securityUser.isCredentialsNonExpired());
        dto.setEnabled(securityUser.isEnabled());
        return dto;
    }
}