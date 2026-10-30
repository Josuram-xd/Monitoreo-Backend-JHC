package com.monitoring.service;

import com.monitoring.MicroserviceProxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderService implements MicroserviceProxy<Object> {

    private record Order(String sku, int qty, String status) {
    }

    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger();

    @Override
    public Object execute(String operation, Object... params) {
        return switch (operation) {
            case "create" -> create(String.valueOf(params[0]), ((Number) params[1]).intValue());
            case "get" -> find(String.valueOf(params[0])).toString();
            case "cancel" -> cancel(String.valueOf(params[0]));
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        };
    }

    private String create(String sku, int qty) {
        String id = "ORD-" + sequence.incrementAndGet();
        orders.put(id, new Order(sku, qty, "CREATED"));
        return id;
    }

    private String cancel(String id) {
        Order order = find(id);
        orders.put(id, new Order(order.sku(), order.qty(), "CANCELLED"));
        return "CANCELLED";
    }

    private Order find(String id) {
        Order order = orders.get(id);
        if (order == null) {
            throw new IllegalArgumentException("Unknown order: " + id);
        }
        return order;
    }
}
