package com.a2a.kafka.agents.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.stream.Collectors;

public final class AuditLogger {
    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    private AuditLogger() {}

    public static void log(String action, String agentName, Map<String, ?> details) {
        String correlationId = MDC.get("correlationId");
        String payload = (details == null || details.isEmpty()) ? "" : details.entrySet().stream()
                .map(e -> e.getKey() + "=" + String.valueOf(e.getValue()))
                .collect(Collectors.joining(","));
        audit.info("action={} agent={} correlationId={} details={}", action, agentName, correlationId, payload);
    }
}
