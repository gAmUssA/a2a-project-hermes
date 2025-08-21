package com.a2a.kafka.agents.registry;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public class AgentRegistryEntry {
    private final String agentName;
    private final Map<String, Object> metadata;
    private final Instant updatedAt;

    public AgentRegistryEntry(String agentName, Map<String, Object> metadata, Instant updatedAt) {
        this.agentName = agentName;
        this.metadata = metadata == null ? Collections.emptyMap() : Collections.unmodifiableMap(metadata);
        this.updatedAt = updatedAt == null ? Instant.now() : updatedAt;
    }

    public String getAgentName() {
        return agentName;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}