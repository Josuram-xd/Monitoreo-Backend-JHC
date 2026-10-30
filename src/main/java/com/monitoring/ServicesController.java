package com.monitoring;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServicesController {

    private final ProxyRegistry registry;

    public ServicesController(ProxyRegistry registry) {
        this.registry = registry;
    }

    @PostMapping("/api/services/{service}/{operation}")
    public Map<String, Object> call(@PathVariable String service,
                                    @PathVariable String operation,
                                    @RequestBody(required = false) List<Object> body) {
        List<Object> params = Objects.requireNonNullElse(body, List.of());
        return Map.of("result", registry.call(service, operation, params.toArray()));
    }
}
