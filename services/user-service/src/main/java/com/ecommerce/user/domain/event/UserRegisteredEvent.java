// services/user-service/src/main/java/com/ecommerce/user/domain/event/UserRegisteredEvent.java
package com.ecommerce.user.domain.event;

import com.ecommerce.domain.common.event.DomainEvent;
import com.ecommerce.domain.common.valueobject.UserId;
import com.ecommerce.user.domain.valueobject.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserRegisteredEvent extends DomainEvent {
    
    private final UserId userId;
    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final UserRole role;
    
    public UserRegisteredEvent(UserId userId, String username, String email,
                              String firstName, String lastName, UserRole role) {
        super();
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
    
    @Override
    public String getAggregateId() {
        return userId.getValue();
    }
}
