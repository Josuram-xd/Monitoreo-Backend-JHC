package com.monitoring;

import com.monitoring.MetricsService.ServiceSummary;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsServiceTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    private final LogRepository repository = new LogRepository();
    private final MetricsService metrics = new MetricsService(repository, new ProxyRegistry(repository));

    @Test
    void filtersByServiceStatusAndTime() {
        repository.save(entry("inventory", "SUCCESS", 10, T0));
        repository.save(entry("inventory", "ERROR", 10, T0.plusSeconds(3600)));
        repository.save(entry("orders", "ERROR", 10, T0));

        assertEquals(1, repository.findFiltered("inventory", "ERROR", null, null).size());
        assertEquals(2, repository.findFiltered("inventory", null, null, null).size());
        assertEquals(1, repository.findFiltered(null, null, T0.plusSeconds(60), null).size());
    }

    @Test
    void returnsNewestFirst() {
        repository.save(entry("inventory", "SUCCESS", 1, T0));
        repository.save(entry("inventory", "SUCCESS", 2, T0.plusSeconds(1)));

        assertEquals(2, repository.findFiltered(null, null, null, null).get(0).durationMs());
    }

    @Test
    void keepsOnlyLast1000Entries() {
        for (int i = 0; i < 1005; i++) {
            repository.save(entry("orders", "SUCCESS", i, T0));
        }
        List<LogEntry> all = repository.findFiltered(null, null, null, null);

        assertEquals(1000, all.size());
        assertEquals(1004, all.get(0).durationMs());
    }

    @Test
    void summarizesErrorRateAndAverage() {
        repository.save(entry("inventory", "SUCCESS", 10, T0));
        repository.save(entry("inventory", "SUCCESS", 20, T0));
        repository.save(entry("inventory", "ERROR", 30, T0));

        ServiceSummary inventory = metrics.summary().get(0);
        assertEquals("inventory", inventory.serviceId());
        assertEquals(3, inventory.totalCalls());
        assertEquals(33.3, inventory.errorRate(), 0.001);
        assertEquals(20.0, inventory.avgDurationMs(), 0.001);
    }

    @Test
    void returnsZerosForServiceWithoutLogs() {
        ServiceSummary orders = metrics.summary().get(1);

        assertEquals("orders", orders.serviceId());
        assertEquals(0, orders.totalCalls());
        assertEquals(0.0, orders.errorRate());
        assertEquals(0.0, orders.avgDurationMs());
    }

    private static LogEntry entry(String service, String status, long durationMs, Instant timestamp) {
        return new LogEntry("req", service, "op", durationMs, status, timestamp, null, null, null);
    }
}
