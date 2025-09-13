// services/notification-service/src/main/java/com/ecommerce/notification/domain/valueobject/NotificationStatus.java
package com.ecommerce.notification.domain.valueobject;

public enum NotificationStatus {
    PENDING,
    SCHEDULED,
    SENT,
    DELIVERED,
    READ,
    FAILED
}