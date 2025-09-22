package com.signly.application.user.mapper;

import com.signly.application.user.dto.UserResponse;
import com.signly.domain.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUserId().getValue(),
                user.getEmail().getValue(),
                user.getName(),
                user.getCompanyName(),
                user.getUserType(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}