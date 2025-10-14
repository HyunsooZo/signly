package com.signly.user.application.mapper;

import com.signly.user.application.dto.UserResponse;
import com.signly.user.domain.model.Company;
import com.signly.user.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {

    public UserResponse toResponse(User user) {
        Company company = user.getCompany();
        return new UserResponse(
                user.getUserId().getValue(),
                user.getEmail().getValue(),
                user.getName(),
                company != null ? company.getName() : null,
                company != null ? company.getPhone() : null,
                company != null ? company.getAddress() : null,
                user.getUserType(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}