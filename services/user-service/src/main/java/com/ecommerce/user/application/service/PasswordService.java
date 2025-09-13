// services/user-service/src/main/java/com/ecommerce/user/application/service/PasswordService.java
package com.ecommerce.user.application.service;

public interface PasswordService {
    
    String hashPassword(String plainPassword);
    
    boolean verifyPassword(String plainPassword, String hashedPassword);
    
    String generateRandomPassword();
}