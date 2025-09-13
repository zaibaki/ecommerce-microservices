// services/user-service/src/main/java/com/ecommerce/user/application/service/JwtService.java
package com.ecommerce.user.application.service;

import com.ecommerce.user.application.dto.JwtTokens;
import com.ecommerce.user.domain.entity.User;
import reactor.core.publisher.Mono;

public interface JwtService {
    
    Mono<JwtTokens> generateTokens(User user);
    
    Mono<String> refreshAccessToken(String refreshToken);
    
    Mono<Boolean> validateToken(String token);
    
    Mono<String> getUserIdFromToken(String token);
}