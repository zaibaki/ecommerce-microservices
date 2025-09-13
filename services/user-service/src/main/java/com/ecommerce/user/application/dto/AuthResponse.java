// services/user-service/src/main/java/com/ecommerce/user/application/dto/AuthResponse.java
package com.ecommerce.user.application.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserResponse user;
    
    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, UserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}