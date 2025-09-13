// services/user-service/src/main/java/com/ecommerce/user/infrastructure/web/mapper/UserMapper.java
package com.ecommerce.user.infrastructure.web.mapper;

import com.ecommerce.user.application.dto.UserResponse;
import com.ecommerce.user.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUserId(user.getUserId().getValue());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setStatus(user.getStatus());
        response.setRole(user.getRole());
        response.setEmailVerified(user.getEmailVerified());
        response.setPhoneVerified(user.getPhoneVerified());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
