// services/user-service/src/main/java/com/ecommerce/user/infrastructure/repository/R2dbcUserRepository.java
package com.ecommerce.user.infrastructure.repository;

import com.ecommerce.domain.common.valueobject.UserId;
import com.ecommerce.user.domain.entity.User;
import com.ecommerce.user.domain.repository.UserRepository;
import com.ecommerce.user.domain.valueobject.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
@Slf4j
public class R2dbcUserRepository implements UserRepository {
    
    private final R2dbcEntityTemplate template;
    private final ReactiveRedisTemplate<String, User> redisTemplate;
    
    private static final String CACHE_PREFIX = "user:";
    private static final Duration CACHE_TTL = Duration.ofHours(2);
    
    @Override
    public Mono<User> save(User user) {
        return template.insert(user)
            .flatMap(savedUser -> cacheUser(savedUser).then(Mono.just(savedUser)))
            .doOnSuccess(savedUser -> log.debug("User saved: {}", savedUser.getUsername()))
            .doOnError(error -> log.error("Failed to save user: {}", user.getUsername(), error));
    }
    
    @Override
    public Mono<User> findById(String id) {
        String cacheKey = CACHE_PREFIX + id;
        
        return redisTemplate.opsForValue().get(cacheKey)
            .doOnNext(cached -> log.debug("User found in cache: {}", id))
            .switchIfEmpty(
                template.selectOne(Query.query(Criteria.where("id").is(id)), User.class)
                    .flatMap(user -> cacheUser(user).then(Mono.just(user)))
                    .doOnNext(user -> log.debug("User loaded from database: {}", id))
            )
            .doOnError(error -> log.error("Failed to find user by id: {}", id, error));
    }
    
    @Override
    public Mono<User> findByUserId(UserId userId) {
        Query query = Query.query(Criteria.where("user_id").is(userId.getValue()));
        
        return template.selectOne(query, User.class)
            .flatMap(user -> cacheUser(user).then(Mono.just(user)))
            .doOnSuccess(user -> log.debug("User found by userId: {}", userId))
            .doOnError(error -> log.error("Failed to find user by userId: {}", userId, error));
    }
    
    @Override
    public Mono<User> findByUsername(String username) {
        Query query = Query.query(Criteria.where("username").is(username));
        
        return template.selectOne(query, User.class)
            .flatMap(user -> cacheUser(user).then(Mono.just(user)))
            .doOnSuccess(user -> log.debug("User found by username: {}", username))
            .doOnError(error -> log.error("Failed to find user by username: {}", username, error));
    }
    
    @Override
    public Mono<User> findByEmail(String email) {
        Query query = Query.query(Criteria.where("email").is(email.toLowerCase()));
        
        return template.selectOne(query, User.class)
            .flatMap(user -> cacheUser(user).then(Mono.just(user)))
            .doOnSuccess(user -> log.debug("User found by email: {}", email))
            .doOnError(error -> log.error("Failed to find user by email: {}", email, error));
    }
    
    @Override
    public Mono<User> findByEmailVerificationToken(String token) {
        Query query = Query.query(Criteria.where("email_verification_token").is(token));
        
        return template.selectOne(query, User.class)
            .doOnSuccess(user -> log.debug("User found by email verification token"))
            .doOnError(error -> log.error("Failed to find user by email verification token", error));
    }
    
    @Override
    public Mono<User> findByPasswordResetToken(String token) {
        Query query = Query.query(Criteria.where("password_reset_token").is(token));
        
        return template.selectOne(query, User.class)
            .doOnSuccess(user -> log.debug("User found by password reset token"))
            .doOnError(error -> log.error("Failed to find user by password reset token", error));
    }
    
    @Override
    public Flux<User> findByStatus(UserStatus status) {
        Query query = Query.query(Criteria.where("status").is(status));
        
        return template.select(query, User.class)
            .doOnNext(user -> log.debug("User found with status: {}", status))
            .doOnError(error -> log.error("Failed to find users by status: {}", status, error));
    }
    
    @Override
    public Mono<Boolean> existsByUsername(String username) {
        Query query = Query.query(Criteria.where("username").is(username));
        
        return template.exists(query, User.class)
            .doOnSuccess(exists -> log.debug("Username exists check for {}: {}", username, exists))
            .doOnError(error -> log.error("Failed to check username existence: {}", username, error));
    }
    
    @Override
    public Mono<Boolean> existsByEmail(String email) {
        Query query = Query.query(Criteria.where("email").is(email.toLowerCase()));
        
        return template.exists(query, User.class)
            .doOnSuccess(exists -> log.debug("Email exists check for {}: {}", email, exists))
            .doOnError(error -> log.error("Failed to check email existence: {}", email, error));
    }
    
    @Override
    public Mono<Void> deleteById(String id) {
        return template.delete(Query.query(Criteria.where("id").is(id)), User.class)
            .then(invalidateCache(id))
            .doOnSuccess(unused -> log.debug("User deleted: {}", id))
            .doOnError(error -> log.error("Failed to delete user: {}", id, error));
    }
    
    @Override
    public Mono<Long> countByStatus(UserStatus status) {
        Query query = Query.query(Criteria.where("status").is(status));
        
        return template.count(query, User.class)
            .doOnSuccess(count -> log.debug("Count for status {}: {}", status, count))
            .doOnError(error -> log.error("Failed to count users by status: {}", status, error));
    }
    
    private Mono<Void> cacheUser(User user) {
        String cacheKey = CACHE_PREFIX + user.getId();
        return redisTemplate.opsForValue()
            .set(cacheKey, user, CACHE_TTL)
            .then()
            .onErrorResume(error -> {
                log.warn("Failed to cache user: {}", user.getId(), error);
                return Mono.empty();
            });
    }
    
    private Mono<Void> invalidateCache(String id) {
        String cacheKey = CACHE_PREFIX + id;
        return redisTemplate.delete(cacheKey)
            .then()
            .onErrorResume(error -> {
                log.warn("Failed to invalidate cache for user: {}", id, error);
                return Mono.empty();
            });
    }
}
