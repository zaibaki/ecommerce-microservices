// services/notification-service/src/main/java/com/ecommerce/notification/domain/entity/Notification.java
package com.ecommerce.notification.domain.entity;

import com.ecommerce.domain.common.entity.BaseEntity;
import com.ecommerce.domain.common.valueobject.UserId;
import com.ecommerce.notification.domain.valueobject.NotificationChannel;
import com.ecommerce.notification.domain.valueobject.NotificationStatus;
import com.ecommerce.notification.domain.valueobject.NotificationType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "notifications")
public class Notification extends BaseEntity {
    
    @NotNull
    private UserId recipientId;
    
    @NotNull
    private NotificationType type;
    
    @NotNull
    private NotificationChannel channel;
    
    @NotBlank
    private String subject;
    
    @NotBlank
    private String content;
    
    private String templateId;
    
    private Map<String, Object> templateData;
    
    @NotNull
    private NotificationStatus status;
    
    private String recipient; // email, phone, or user ID for in-app
    
    private Instant scheduledAt;
    
    private Instant sentAt;
    
    private Instant deliveredAt;
    
    private Instant readAt;
    
    private String failureReason;
    
    private Integer retryCount;
    
    private String externalMessageId;
    
    protected Notification() {
        super();
    }
    
    public Notification(UserId recipientId, NotificationType type, NotificationChannel channel,
                       String subject, String content, String recipient) {
        super();
        this.recipientId = recipientId;
        this.type = type;
        this.channel = channel;
        this.subject = subject;
        this.content = content;
        this.recipient = recipient;
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
    }
    
    public void markSent(String externalMessageId) {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
        this.externalMessageId = externalMessageId;
    }
    
    public void markDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = Instant.now();
    }
    
    public void markRead() {
        this.status = NotificationStatus.READ;
        this.readAt = Instant.now();
    }
    
    public void markFailed(String failureReason) {
        this.status = NotificationStatus.FAILED;
        this.failureReason = failureReason;
        this.retryCount++;
    }
    
    public void schedule(Instant scheduledTime) {
        this.scheduledAt = scheduledTime;
        this.status = NotificationStatus.SCHEDULED;
    }
    
    public boolean canRetry() {
        return status == NotificationStatus.FAILED && retryCount < 3;
    }
    
    public boolean isReadyToSend() {
        return status == NotificationStatus.PENDING || 
               (status == NotificationStatus.SCHEDULED && 
                scheduledAt != null && scheduledAt.isBefore(Instant.now()));
    }
}
