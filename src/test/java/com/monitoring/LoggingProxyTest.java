package com.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoggingProxyTest {

    private final LogRepository repo = new LogRepository();

    @Test
    void logsSuccessfulCall() {
        MicroserviceProxy<Object> target = (operation, params) -> "ok";
        LoggingProxy<Object> proxy = new LoggingProxy<>("inventory", target, repo);

        assertEquals("ok", proxy.execute("getStock", "SKU-1"));

        LogEntry entry = captureSavedEntry();
        assertEquals("SUCCESS", entry.status());
        assertEquals("inventory", entry.serviceId());
        assertEquals("getStock", entry.operation());
        assertEquals("[SKU-1]", entry.params());
        assertEquals("ok", entry.response());
        assertNull(entry.error());
    }

    @Test
    void logsFailedCallAndRethrows() {
        MicroserviceProxy<Object> target = (operation, params) -> {
            throw new IllegalStateException("boom");
        };
        LoggingProxy<Object> proxy = new LoggingProxy<>("payments", target, repo);

        assertThrows(IllegalStateException.class, () -> proxy.execute("charge"));

        LogEntry entry = captureSavedEntry();
        assertEquals("ERROR", entry.status());
        assertNull(entry.response());
        assertTrue(entry.error().startsWith("java.lang.IllegalStateException: boom"));
    }

    private LogEntry captureSavedEntry() {
        return repo.findFiltered(null, null, null, null).get(0);
    }
}
