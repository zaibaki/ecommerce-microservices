// services/user-service/src/main/java/com/ecommerce/user/infrastructure/web/util/IpAddressUtil.java
package com.ecommerce.user.infrastructure.web.util;

import org.springframework.http.server.reactive.ServerHttpRequest;

public class IpAddressUtil {
    
    public static String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null 
            ? request.getRemoteAddress().getAddress().getHostAddress() 
            : "unknown";
    }
}