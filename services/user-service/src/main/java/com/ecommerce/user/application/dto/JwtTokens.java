// services/user-service/src/main/java/com/ecommerce/user/application/dto/JwtTokens.java
package com.ecommerce.user.application.dto;

import lombok.Data;

@Data
public class JwtTokens {
    
    private final String accessToken;
    private final String refreshToken;
    private final Long expiresIn;
    
    public JwtTokens(String accessToken, String refreshToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
}