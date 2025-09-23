package com.signly.user.application.mapper;

import com.signly.user.application.dto.UserResponse;
import com.signly.user.domain.model.User;
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