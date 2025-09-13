// services/user-service/src/main/java/com/ecommerce/user/domain/event/UserLoggedInEvent.java
package com.ecommerce.user.domain.event;

import com.ecommerce.domain.common.event.DomainEvent;
import com.ecommerce.domain.common.valueobject.UserId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserLoggedInEvent extends DomainEvent {
    
    private final UserId userId;
    private final String username;
    private final String ipAddress;
    private final String userAgent;
    
    public UserLoggedInEvent(UserId userId, String username, String ipAddress, String userAgent) {
        super();
        this.userId = userId;
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    @Override
    public String getAggregateId() {
        return userId.getValue();
    }
}