// services/user-service/src/main/java/com/ecommerce/user/application/dto/LoginRequest.java
package com.ecommerce.user.application.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    
    @NotBlank
    private String usernameOrEmail;
    
    @NotBlank
    private String password;
    
    private boolean rememberMe = false;
}
