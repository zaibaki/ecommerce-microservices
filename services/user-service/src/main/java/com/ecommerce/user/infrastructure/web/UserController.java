// services/user-service/src/main/java/com/ecommerce/user/infrastructure/web/UserController.java
package com.ecommerce.user.infrastructure.web;

import com.ecommerce.user.application.dto.AuthResponse;
import com.ecommerce.user.application.dto.LoginRequest;
import com.ecommerce.user.application.dto.RegisterUserRequest;
import com.ecommerce.user.application.dto.UserResponse;
import com.ecommerce.user.application.usecase.AuthenticateUserUseCase;
import com.ecommerce.user.application.usecase.RegisterUserUseCase;
import com.ecommerce.user.infrastructure.web.mapper.UserMapper;
import com.ecommerce.user.infrastructure.web.util.IpAddressUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Authentication", description = "User authentication and registration operations")
public class UserController {
    
    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final UserMapper userMapper;
    
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data or user already exists")
    public Mono<ResponseEntity<UserResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        log.info("User registration request for: {}", request.getUsername());
        
        return registerUserUseCase.execute(request)
            .map(userMapper::toResponse)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
            .doOnSuccess(response -> log.info("User registered successfully: {}", 
                response.getBody().getUsername()))
            .doOnError(error -> log.error("Failed to register user: {}", 
                request.getUsername(), error));
    }
    
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates user and returns JWT tokens")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials or account locked")
    public Mono<ResponseEntity<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            ServerHttpRequest httpRequest) {
        
        log.info("Login request for: {}", request.getUsernameOrEmail());
        
        String ipAddress = IpAddressUtil.getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeaders().getFirst("User-Agent");
        
        return authenticateUserUseCase.execute(request, ipAddress, userAgent)
            .map(ResponseEntity::ok)
            .doOnSuccess(response -> log.info("User authenticated successfully: {}", 
                response.getBody().getUser().getUsername()))
            .doOnError(error -> log.warn("Authentication failed for: {}", 
                request.getUsernameOrEmail(), error));
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates new access token using refresh token")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    public Mono<ResponseEntity<String>> refreshToken(@RequestParam String refreshToken) {
        // Implementation for token refresh
        return Mono.just(ResponseEntity.ok("Token refreshed"));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates user tokens")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    public Mono<ResponseEntity<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        // Implementation for logout
        return Mono.just(ResponseEntity.ok().build());
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Sends password reset email")
    @ApiResponse(responseCode = "200", description = "Password reset email sent")
    public Mono<ResponseEntity<Void>> forgotPassword(@RequestParam String email) {
        // Implementation for password reset request
        return Mono.just(ResponseEntity.ok().build());
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets password using reset token")
    @ApiResponse(responseCode = "200", description = "Password reset successful")
    public Mono<ResponseEntity<Void>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        // Implementation for password reset
        return Mono.just(ResponseEntity.ok().build());
    }
    
    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verifies user email using verification token")
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    public Mono<ResponseEntity<Void>> verifyEmail(@RequestParam String token) {
        // Implementation for email verification
        return Mono.just(ResponseEntity.ok().build());
    }
}