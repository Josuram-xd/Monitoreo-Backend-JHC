package com.monitoring;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MetricsService {

    public record ServiceSummary(String serviceId, long totalCalls, double errorRate, double avgDurationMs) {}

    public record LogPage(List<LogEntry> items, int page, int size, int total) {}

    private static final List<String> SERVICES = List.of("inventory", "orders", "payments");
    private static final int SIMULATED_CALLS = 50;

    private final LogRepository repository;
    private final ProxyRegistry registry;

    public MetricsService(LogRepository repository, ProxyRegistry registry) {
        this.repository = repository;
        this.registry = registry;
    }

    public List<ServiceSummary> summary() {
        return SERVICES.stream()
                .map(id -> summarize(id, repository.findFiltered(id, null, null, null)))
                .toList();
    }

    public LogPage logs(String service, String status, Instant from, Instant to, int page, int size) {
        List<LogEntry> all = repository.findFiltered(service, status, from, to);
        List<LogEntry> items = all.stream().skip((long) page * size).limit(size).toList();
        return new LogPage(items, page, size, all.size());
    }

    public void clearLogs() {
        repository.clear();
    }

    public int simulateLoad() {
        for (int i = 0; i < SIMULATED_CALLS; i++) {
            try {
                randomCall().run();
            } catch (Exception ignored) {
                // the proxy already logs failed calls
            }
        }
        return SIMULATED_CALLS;
    }

    private static ServiceSummary summarize(String serviceId, List<LogEntry> logs) {
        long total = logs.size();
        long errors = logs.stream().filter(e -> "ERROR".equals(e.status())).count();
        double errorRate = total == 0 ? 0 : round1(100.0 * errors / total);
        double avg = logs.stream().mapToLong(LogEntry::durationMs).average().orElse(0);
        return new ServiceSummary(serviceId, total, errorRate, round1(avg));
    }

    private Runnable randomCall() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String sku = "SKU-" + r.nextInt(1, 6);
        String orderId = "ORD-" + r.nextInt(1, 6);
        int qty = r.nextInt(1, 6);
        List<Runnable> calls = List.of(
                () -> registry.call("inventory", "getStock", sku),
                () -> registry.call("inventory", "reserve", sku, qty),
                () -> registry.call("inventory", "restock", sku, qty),
                () -> registry.call("orders", "create", sku, qty),
                () -> registry.call("payments", "charge", orderId, r.nextInt(10, 501)),
                () -> registry.call("payments", "refund", orderId));
        return calls.get(r.nextInt(calls.size()));
    }

    private static double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
