package com.monitoring;

import java.time.Instant;

public record LogEntry(
        String requestId,
        String serviceId,
        String operation,
        long durationMs,
        String status,
        Instant timestamp,
        String params,
        String response,
        String error) {
}
