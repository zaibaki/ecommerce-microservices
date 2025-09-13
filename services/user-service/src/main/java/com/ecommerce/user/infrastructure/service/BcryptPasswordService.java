// services/user-service/src/main/java/com/ecommerce/user/infrastructure/service/BcryptPasswordService.java
package com.ecommerce.user.infrastructure.service;

import com.ecommerce.user.application.service.PasswordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@Slf4j
public class BcryptPasswordService implements PasswordService {
    
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    
    public BcryptPasswordService() {
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.secureRandom = new SecureRandom();
    }
    
    @Override
    public String hashPassword(String plainPassword) {
        log.debug("Hashing password");
        return passwordEncoder.encode(plainPassword);
    }
    
    @Override
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        log.debug("Verifying password");
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
    
    @Override
    public String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*?&";
        StringBuilder password = new StringBuilder(12);
        
        for (int i = 0; i < 12; i++) {
            password.append(characters.charAt(secureRandom.nextInt(characters.length())));
        }
        
        return password.toString();
    }
}