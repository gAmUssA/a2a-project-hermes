package com.a2a.kafka.agents.core;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public class AgentHealth {
    private final AgentHealthStatus status;
    private final Map<String, Object> details;
    private final String lastErrorMessage;
    private final Instant timestamp;

    public AgentHealth(AgentHealthStatus status) {
        this(status, Collections.emptyMap(), null, Instant.now());
    }

    public AgentHealth(AgentHealthStatus status, Map<String, Object> details) {
        this(status, details, null, Instant.now());
    }

    public AgentHealth(AgentHealthStatus status, Map<String, Object> details, String lastErrorMessage, Instant timestamp) {
        this.status = status;
        this.details = details == null ? Collections.emptyMap() : Collections.unmodifiableMap(details);
        this.lastErrorMessage = lastErrorMessage;
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public AgentHealthStatus getStatus() {
        return status;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
