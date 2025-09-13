// services/user-service/src/main/java/com/ecommerce/user/domain/entity/User.java
package com.ecommerce.user.domain.entity;

import com.ecommerce.domain.common.entity.BaseEntity;
import com.ecommerce.domain.common.exception.BusinessRuleViolationException;
import com.ecommerce.domain.common.util.ValidationUtils;
import com.ecommerce.domain.common.valueobject.UserId;
import com.ecommerce.user.domain.valueobject.UserRole;
import com.ecommerce.user.domain.valueobject.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("users")
public class User extends BaseEntity {
    
    @NotNull
    @Column("user_id")
    private UserId userId;
    
    @NotBlank
    @Column("username")
    private String username;
    
    @NotBlank
    @Email
    @Column("email")
    private String email;
    
    @NotBlank
    @Column("password_hash")
    private String passwordHash;
    
    @NotBlank
    @Column("first_name")
    private String firstName;
    
    @NotBlank
    @Column("last_name")
    private String lastName;
    
    @Column("phone_number")
    private String phoneNumber;
    
    @NotNull
    @Column("status")
    private UserStatus status;
    
    @NotNull
    @Column("role")
    private UserRole role;
    
    @Column("email_verified")
    private Boolean emailVerified;
    
    @Column("phone_verified")
    private Boolean phoneVerified;
    
    @Column("last_login_at")
    private Instant lastLoginAt;
    
    @Column("failed_login_attempts")
    private Integer failedLoginAttempts;
    
    @Column("locked_until")
    private Instant lockedUntil;
    
    @Column("email_verification_token")
    private String emailVerificationToken;
    
    @Column("password_reset_token")
    private String passwordResetToken;
    
    @Column("password_reset_expires_at")
    private Instant passwordResetExpiresAt;
    
    @Transient
    private Set<String> permissions;
    
    protected User() {
        super();
    }
    
    public User(UserId userId, String username, String email, String passwordHash,
                String firstName, String lastName, String phoneNumber, UserRole role) {
        super();
        validateUserData(username, email, firstName, lastName);
        
        this.userId = userId;
        this.username = username;
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = UserStatus.ACTIVE;
        this.emailVerified = false;
        this.phoneVerified = false;
        this.failedLoginAttempts = 0;
    }
    
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
    }
    
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
    
    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }
    
    public void lock(Instant until) {
        this.status = UserStatus.LOCKED;
        this.lockedUntil = until;
    }
    
    public void recordSuccessfulLogin() {
        this.lastLoginAt = Instant.now();
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        if (this.status == UserStatus.LOCKED) {
            this.status = UserStatus.ACTIVE;
        }
    }
    
    public void recordFailedLogin() {
        this.failedLoginAttempts = (this.failedLoginAttempts != null ? this.failedLoginAttempts : 0) + 1;
        
        if (this.failedLoginAttempts >= 5) {
            lock(Instant.now().plusSeconds(300)); // Lock for 5 minutes
        }
    }
    
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
    }
    
    public void verifyPhone() {
        this.phoneVerified = true;
    }
    
    public void setPasswordResetToken(String token, Instant expiresAt) {
        this.passwordResetToken = token;
        this.passwordResetExpiresAt = expiresAt;
    }
    
    public void resetPassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.passwordResetToken = null;
        this.passwordResetExpiresAt = null;
        this.failedLoginAttempts = 0;
        if (this.status == UserStatus.LOCKED) {
            this.status = UserStatus.ACTIVE;
        }
    }
    
    public boolean isLocked() {
        return status == UserStatus.LOCKED && 
               (lockedUntil == null || lockedUntil.isAfter(Instant.now()));
    }
    
    public boolean isActive() {
        return status == UserStatus.ACTIVE && !isLocked();
    }
    
    public boolean canLogin() {
        return isActive() && status != UserStatus.SUSPENDED;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    private void validateUserData(String username, String email, String firstName, String lastName) {
        ValidationUtils.requireNonEmpty(username, "username");
        ValidationUtils.requireNonEmpty(firstName, "firstName");
        ValidationUtils.requireNonEmpty(lastName, "lastName");
        
        if (!ValidationUtils.isValidEmail(email)) {
            throw new BusinessRuleViolationException("Invalid email format: " + email);
        }
        
        if (username.length() < 3 || username.length() > 50) {
            throw new BusinessRuleViolationException("Username must be between 3 and 50 characters");
        }
        
        if (!username.matches("^[a-zA-Z0-9_.-]+$")) {
            throw new BusinessRuleViolationException("Username can only contain letters, numbers, dots, hyphens, and underscores");
        }
    }
}