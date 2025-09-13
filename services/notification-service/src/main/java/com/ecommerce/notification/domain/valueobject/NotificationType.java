// services/notification-service/src/main/java/com/ecommerce/notification/domain/valueobject/NotificationType.java
package com.ecommerce.notification.domain.valueobject;

public enum NotificationType {
    ORDER_CONFIRMATION,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    PAYMENT_SUCCESSFUL,
    PAYMENT_FAILED,
    ACCOUNT_CREATED,
    PASSWORD_RESET,
    PROMOTIONAL,
    LOW_STOCK_ALERT,
    FRAUD_ALERT,
    SYSTEM_MAINTENANCE
}