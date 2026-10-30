package com.monitoring.service;

import com.monitoring.MicroserviceProxy;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PaymentService implements MicroserviceProxy<Object> {

    @Override
    public Object execute(String operation, Object... params) {
        return switch (operation) {
            case "charge" -> charge();
            case "refund" -> "REFUNDED";
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        };
    }

    private String charge() {
        if (ThreadLocalRandom.current().nextInt(10) == 0) {
            throw new IllegalStateException("Payment gateway unavailable");
        }
        return "PAID-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
