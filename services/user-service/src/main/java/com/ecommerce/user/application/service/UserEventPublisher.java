// services/user-service/src/main/java/com/ecommerce/user/application/service/UserEventPublisher.java
package com.ecommerce.user.application.service;

import com.ecommerce.user.domain.event.UserLoggedInEvent;
import com.ecommerce.user.domain.event.UserRegisteredEvent;

public interface UserEventPublisher {
    
    void publishUserRegistered(UserRegisteredEvent event);
    
    void publishUserLoggedIn(UserLoggedInEvent event);
}