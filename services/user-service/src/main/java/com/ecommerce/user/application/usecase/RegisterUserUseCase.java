package com.ecommerce.user.application.usecase;

import com.ecommerce.domain.common.exception.BusinessRuleViolationException;
import com.ecommerce.domain.common.valueobject.UserId;
import com.ecommerce.user.application.dto.RegisterUserRequest;
import com.ecommerce.user.application.service.UserEventPublisher;
import com.ecommerce.user.application.service.PasswordService;
import com.ecommerce.user.domain.entity.User;
import com.ecommerce.user.domain.event.UserRegisteredEvent;
import com.ecommerce.user.domain.repository.UserRepository;
import com.ecommerce.user.domain.valueobject.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterUserUseCase {
    
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final UserEventPublisher eventPublisher;
    
    public Mono<User> execute(RegisterUserRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        return validateUniqueUserData(request)
            .then(Mono.fromSupplier(() -> createUser(request)))
            .flatMap(userRepository::save)
            .doOnSuccess(user -> {
                log.info("User registered successfully: {}", user.getUsername());
                publishUserRegisteredEvent(user);
            })
            .doOnError(error -> log.error("Failed to register user: {}", request.getUsername(), error));
    }
    
    private Mono<Void> validateUniqueUserData(RegisterUserRequest request) {
        return Mono.zip(
            userRepository.existsByUsername(request.getUsername()),
            userRepository.existsByEmail(request.getEmail())
        )
        .flatMap(tuple -> {
            boolean usernameExists = tuple.getT1();
            boolean emailExists = tuple.getT2();
            
            if (usernameExists) {
                return Mono.error(new BusinessRuleViolationException("Username already exists: " + request.getUsername()));
            }
            if (emailExists) {
                return Mono.error(new BusinessRuleViolationException("Email already exists: " + request.getEmail()));
            }
            return Mono.empty();
        });
    }
    
    private User createUser(RegisterUserRequest request) {
        UserId userId = UserId.generate();
        String passwordHash = passwordService.hashPassword(request.getPassword());
        
        return new User(
            userId,
            request.getUsername(),
            request.getEmail(),
            passwordHash,
            request.getFirstName(),
            request.getLastName(),
            request.getPhoneNumber(),
            UserRole.CUSTOMER
        );
    }
    
    private void publishUserRegisteredEvent(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole()
        );
        eventPublisher.publishUserRegistered(event);
    }
}
