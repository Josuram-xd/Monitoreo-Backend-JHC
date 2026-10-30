package com.monitoring;

import com.monitoring.MetricsService.LogPage;
import com.monitoring.MetricsService.ServiceSummary;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metrics;

    public MetricsController(MetricsService metrics) {
        this.metrics = metrics;
    }

    @GetMapping("/summary")
    public List<ServiceSummary> summary() {
        return metrics.summary();
    }

    @GetMapping("/logs")
    public LogPage logs(@RequestParam(required = false) String service,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String from,
                        @RequestParam(required = false) String to,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
        return metrics.logs(text(service), text(status), instant(from), instant(to), page, size);
    }

    @DeleteMapping("/logs")
    public Map<String, Boolean> clearLogs() {
        metrics.clearLogs();
        return Map.of("cleared", true);
    }

    @PostMapping("/simulate-load")
    public Map<String, Integer> simulateLoad() {
        return Map.of("generated", metrics.simulateLoad());
    }

    private static String text(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static Instant instant(String value) {
        String text = text(value);
        return text == null ? null : Instant.parse(text);
    }
}
