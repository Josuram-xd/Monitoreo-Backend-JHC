package com.monitoring;

import com.monitoring.service.InventoryService;
import com.monitoring.service.OrderService;
import com.monitoring.service.PaymentService;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class ProxyRegistry {

    private final Map<String, MicroserviceProxy<Object>> proxies;

    public ProxyRegistry(LogRepository repo) {
        this.proxies = Map.of(
                "inventory", new LoggingProxy<>("inventory", withLatency(new InventoryService()), repo),
                "orders", new LoggingProxy<>("orders", withLatency(new OrderService()), repo),
                "payments", new LoggingProxy<>("payments", withLatency(new PaymentService()), repo));
    }

    // Simulated network latency so durations are not all ~0 ms
    private static MicroserviceProxy<Object> withLatency(MicroserviceProxy<Object> service) {
        return (operation, params) -> {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(20, 250));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return service.execute(operation, params);
        };
    }

    public Object call(String serviceId, String operation, Object... params) {
        return Optional.ofNullable(proxies.get(serviceId))
                .orElseThrow(() -> new NoSuchElementException("Unknown service: " + serviceId))
                .execute(operation, params);
    }
}
