package com.monitoring;

import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Repository
public class LogRepository {

    private static final int MAX_ENTRIES = 1000;

    // shortcut: in-memory, lost on restart; upgrade to a DB if persistence is needed
    private final ConcurrentLinkedDeque<LogEntry> entries = new ConcurrentLinkedDeque<>();

    public void save(LogEntry entry) {
        entries.addFirst(entry);
        if (entries.size() > MAX_ENTRIES) {
            entries.pollLast();
        }
    }

    public List<LogEntry> findFiltered(String service, String status, Instant from, Instant to) {
        return entries.stream()
                .filter(e -> service == null || service.equals(e.serviceId()))
                .filter(e -> status == null || status.equals(e.status()))
                .filter(e -> from == null || !e.timestamp().isBefore(from))
                .filter(e -> to == null || !e.timestamp().isAfter(to))
                .toList();
    }
}
