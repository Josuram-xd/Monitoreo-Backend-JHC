package com.monitoring;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class LoggingProxy<T> implements MicroserviceProxy<T> {

    private final String serviceId;
    private final MicroserviceProxy<T> target;
    private final LogRepository repo;

    public LoggingProxy(String serviceId, MicroserviceProxy<T> target, LogRepository repo) {
        this.serviceId = serviceId;
        this.target = target;
        this.repo = repo;
    }

    @Override
    public T execute(String operation, Object... params) {
        String requestId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();
        long start = System.nanoTime();
        String paramsText = Arrays.toString(params);
        try {
            T result = target.execute(operation, params);
            repo.save(new LogEntry(requestId, serviceId, operation, durationSince(start), "SUCCESS",
                    timestamp, paramsText, String.valueOf(result), null));
            return result;
        } catch (RuntimeException e) {
            repo.save(new LogEntry(requestId, serviceId, operation, durationSince(start), "ERROR",
                    timestamp, paramsText, null, summarize(e)));
            throw e;
        }
    }

    private static long durationSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private static String summarize(RuntimeException e) {
        return e + "\n" + Arrays.stream(e.getStackTrace())
                .limit(3)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }
}
