// services/user-service/src/main/java/com/ecommerce/user/domain/repository/UserRepository.java
package com.ecommerce.user.domain.repository;

import com.ecommerce.domain.common.valueobject.UserId;
import com.ecommerce.user.domain.entity.User;
import com.ecommerce.user.domain.valueobject.UserStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
    
    Mono<User> save(User user);
    
    Mono<User> findById(String id);
    
    Mono<User> findByUserId(UserId userId);
    
    Mono<User> findByUsername(String username);
    
    Mono<User> findByEmail(String email);
    
    Mono<User> findByEmailVerificationToken(String token);
    
    Mono<User> findByPasswordResetToken(String token);
    
    Flux<User> findByStatus(UserStatus status);
    
    Mono<Boolean> existsByUsername(String username);
    
    Mono<Boolean> existsByEmail(String email);
    
    Mono<Void> deleteById(String id);
    
    Mono<Long> countByStatus(UserStatus status);
}
