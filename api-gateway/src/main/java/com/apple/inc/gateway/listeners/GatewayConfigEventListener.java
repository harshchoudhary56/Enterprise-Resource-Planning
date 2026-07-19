package com.apple.inc.gateway.listeners;

import com.apple.inc.platform.common.events.GatewayConfigRefreshEvent;
import org.springframework.stereotype.Component;

@Component
public class GatewayConfigEventListener {

    public void handle(GatewayConfigRefreshEvent event) {
        // Log the event reception
        System.out.println("Received GatewayConfigRefreshEvent: " + event);

        // Here you can add logic to refresh any in-memory caches or configurations
        // that depend on the gateway configuration. For example:
        // - Clear route caches
        // - Refresh rate limiter configurations
        // - Update any dynamic filters or predicates

        // For demonstration, we will just log that the configuration has been refreshed.
        System.out.println("Gateway configuration has been refreshed successfully.");
    }
}
