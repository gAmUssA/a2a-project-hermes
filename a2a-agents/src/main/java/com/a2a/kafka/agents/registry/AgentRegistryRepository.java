package com.a2a.kafka.agents.registry;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AgentRegistryRepository {
    private final Map<String, AgentRegistryEntry> store = new ConcurrentHashMap<>();

    public void upsert(AgentRegistryEntry entry) {
        if (entry == null || entry.getAgentName() == null) return;
        store.put(entry.getAgentName(), entry);
    }

    public AgentRegistryEntry findByName(String name) {
        return store.get(name);
    }

    public Collection<AgentRegistryEntry> findAll() {
        return store.values();
    }

    public void clear() {
        store.clear();
    }
}