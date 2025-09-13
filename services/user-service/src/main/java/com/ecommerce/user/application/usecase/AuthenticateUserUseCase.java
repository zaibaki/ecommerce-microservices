//services/user-service/src/main/java/com/ecommerce/user/application/usecase/AuthenticateUserUseCase.java
package com.ecommerce.user.application.usecase;

import com.ecommerce.domain.common.exception.BusinessRuleViolationException;
import com.ecommerce.user.application.dto.AuthResponse;
import com.ecommerce.user.application.dto.LoginRequest;
import com.ecommerce.user.application.dto.UserResponse;
import com.ecommerce.user.application.service.JwtService;
import com.ecommerce.user.application.service.PasswordService;
import com.ecommerce.user.application.service.UserEventPublisher;
import com.ecommerce.user.domain.entity.User;
import com.ecommerce.user.domain.event.UserLoggedInEvent;
import com.ecommerce.user.domain.repository.UserRepository;
import com.ecommerce.user.infrastructure.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticateUserUseCase {
    
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final UserEventPublisher eventPublisher;
    private final UserMapper userMapper;
    
    public Mono<AuthResponse> execute(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Authenticating user: {}", request.getUsernameOrEmail());
        
        return findUserByUsernameOrEmail(request.getUsernameOrEmail())
            .flatMap(user -> validateUserAndPassword(user, request.getPassword()))
            .flatMap(user -> recordSuccessfulLogin(user, ipAddress, userAgent))
            .flatMap(this::generateAuthResponse)
            .doOnSuccess(response -> log.info("User authenticated successfully: {}", 
                response.getUser().getUsername()))
            .doOnError(error -> log.warn("Authentication failed for: {}", 
                request.getUsernameOrEmail(), error));
    }
    
    private Mono<User> findUserByUsernameOrEmail(String usernameOrEmail) {
        // Try to find by username first, then by email
        return userRepository.findByUsername(usernameOrEmail)
            .switchIfEmpty(userRepository.findByEmail(usernameOrEmail))
            .switchIfEmpty(Mono.error(new BusinessRuleViolationException("Invalid credentials")));
    }
    
    private Mono<User> validateUserAndPassword(User user, String password) {
        if (!user.canLogin()) {
            return recordFailedLogin(user)
                .then(Mono.error(new BusinessRuleViolationException("Account is locked or inactive")));
        }
        
        if (!passwordService.verifyPassword(password, user.getPasswordHash())) {
            return recordFailedLogin(user)
                .then(Mono.error(new BusinessRuleViolationException("Invalid credentials")));
        }
        
        return Mono.just(user);
    }
    
    private Mono<User> recordSuccessfulLogin(User user, String ipAddress, String userAgent) {
        user.recordSuccessfulLogin();
        
        return userRepository.save(user)
            .doOnSuccess(savedUser -> publishUserLoggedInEvent(savedUser, ipAddress, userAgent));
    }
    
    private Mono<User> recordFailedLogin(User user) {
        user.recordFailedLogin();
        return userRepository.save(user);
    }
    
    private Mono<AuthResponse> generateAuthResponse(User user) {
        return jwtService.generateTokens(user)
            .map(tokens -> {
                UserResponse userResponse = userMapper.toResponse(user);
                return new AuthResponse(
                    tokens.getAccessToken(),
                    tokens.getRefreshToken(),
                    tokens.getExpiresIn(),
                    userResponse
                );
            });
    }
    
    private void publishUserLoggedInEvent(User user, String ipAddress, String userAgent) {
        UserLoggedInEvent event = new UserLoggedInEvent(
            user.getUserId(),
            user.getUsername(),
            ipAddress,
            userAgent
        );
        eventPublisher.publishUserLoggedIn(event);
    }
}