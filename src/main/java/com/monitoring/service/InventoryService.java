package com.monitoring.service;

import com.monitoring.MicroserviceProxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryService implements MicroserviceProxy<Object> {

    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    public InventoryService() {
        for (int i = 1; i <= 5; i++) {
            stock.put("SKU-" + i, 100);
        }
    }

    @Override
    public Object execute(String operation, Object... params) {
        return switch (operation) {
            case "getStock" -> stock.get(checkSku(String.valueOf(params[0])));
            case "reserve" -> reserve(String.valueOf(params[0]), ((Number) params[1]).intValue());
            case "restock" -> stock.compute(checkSku(String.valueOf(params[0])),
                    (sku, current) -> current + ((Number) params[1]).intValue());
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        };
    }

    private int reserve(String sku, int qty) {
        return stock.compute(checkSku(sku), (k, current) -> {
            if (current < qty) {
                throw new IllegalStateException("Insufficient stock for " + sku);
            }
            return current - qty;
        });
    }

    private String checkSku(String sku) {
        if (!stock.containsKey(sku)) {
            throw new IllegalArgumentException("Unknown SKU: " + sku);
        }
        return sku;
    }
}
