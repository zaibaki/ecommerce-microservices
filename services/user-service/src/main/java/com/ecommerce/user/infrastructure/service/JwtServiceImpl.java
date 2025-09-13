// services/user-service/src/main/java/com/ecommerce/user/infrastructure/service/JwtServiceImpl.java
package com.ecommerce.user.infrastructure.service;

import com.ecommerce.user.application.dto.JwtTokens;
import com.ecommerce.user.application.service.JwtService;
import com.ecommerce.user.domain.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {
    
    private final SecretKey secretKey;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    
    public JwtServiceImpl(@Value("${jwt.secret}") String secret,
                         @Value("${jwt.access-token-expiration:3600}") long accessTokenExpiration,
                         @Value("${jwt.refresh-token-expiration:86400}") long refreshTokenExpiration,
                         ReactiveStringRedisTemplate redisTemplate) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public Mono<JwtTokens> generateTokens(User user) {
        log.debug("Generating JWT tokens for user: {}", user.getUsername());
        
        return Mono.fromCallable(() -> {
            String accessToken = generateAccessToken(user);
            String refreshToken = generateRefreshToken(user);
            
            return new JwtTokens(accessToken, refreshToken, accessTokenExpiration);
        })
        .flatMap(tokens -> storeRefreshToken(user.getUserId().getValue(), tokens.getRefreshToken())
            .then(Mono.just(tokens)));
    }
    
    @Override
    public Mono<String> refreshAccessToken(String refreshToken) {
        return validateRefreshToken(refreshToken)
            .flatMap(userId -> 
                Mono.fromCallable(() -> {
                    Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(refreshToken)
                        .getBody();
                    
                    return Jwts.builder()
                        .setSubject(userId)
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
                        .claim("username", claims.get("username"))
                        .claim("role", claims.get("role"))
                        .signWith(secretKey, SignatureAlgorithm.HS256)
                        .compact();
                })
            );
    }
    
    @Override
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
                return true;
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("Invalid JWT token: {}", e.getMessage());
                return false;
            }
        });
    }
    
    @Override
    public Mono<String> getUserIdFromToken(String token) {
        return Mono.fromCallable(() -> {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            return claims.getSubject();
        });
    }
    
    private String generateAccessToken(User user) {
        return Jwts.builder()
            .setSubject(user.getUserId().getValue())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
            .claim("username", user.getUsername())
            .claim("email", user.getEmail())
            .claim("role", user.getRole().name())
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }
    
    private String generateRefreshToken(User user) {
        return Jwts.builder()
            .setSubject(user.getUserId().getValue())
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration * 1000))
            .claim("username", user.getUsername())
            .claim("role", user.getRole().name())
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }
    
    private Mono<Void> storeRefreshToken(String userId, String refreshToken) {
        String key = "refresh_token:" + userId;
        return redisTemplate.opsForValue()
            .set(key, refreshToken, Duration.ofSeconds(refreshTokenExpiration))
            .then();
    }
    
    private Mono<String> validateRefreshToken(String refreshToken) {
        return Mono.fromCallable(() -> {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
            
            return claims.getSubject();
        })
        .flatMap(userId -> {
            String key = "refresh_token:" + userId;
            return redisTemplate.opsForValue().get(key)
                .filter(storedToken -> storedToken.equals(refreshToken))
                .map(token -> userId)
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid refresh token")));
        });
    }
}