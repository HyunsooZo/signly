package com.deally.user.application.mapper;

import com.deally.user.application.dto.UserResponse;
import com.deally.user.domain.model.Company;
import com.deally.user.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {

    public UserResponse toResponse(User user) {
        Company company = user.getCompany();
        return new UserResponse(
                user.getUserId().value(),
                user.getEmail().value(),
                user.getName(),
                company != null ? company.name() : null,
                company != null ? company.phone() : null,
                company != null ? company.address() : null,
                user.getUserType(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}