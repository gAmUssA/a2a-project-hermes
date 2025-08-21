package com.a2a.kafka.agents.registry;

import com.a2a.kafka.core.message.A2AMessageEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
public class RegistryProcessorsConfig {

    private static final Logger log = LoggerFactory.getLogger(RegistryProcessorsConfig.class);

    private final AgentRegistryRepository repository;

    public RegistryProcessorsConfig(AgentRegistryRepository repository) {
        this.repository = repository;
    }

    @Bean
    public Consumer<A2AMessageEnvelope> registryUpdates() {
        return envelope -> {
            if (envelope == null || envelope.getPayload() == null) {
                return;
            }
            try {
                Object payloadObj = envelope.getPayload();
                if (payloadObj instanceof Map<?, ?> map) {
                    Object agent = map.get("agent");
                    Object metadata = map.get("metadata");
                    if (agent instanceof String name) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> md = metadata instanceof Map ? (Map<String, Object>) metadata : Map.of();
                        repository.upsert(new AgentRegistryEntry(name, md, Instant.now()));
                        log.debug("Registry updated for agent {}", name);
                    }
                }
            } catch (Exception ex) {
                log.warn("Failed to process registry update: {}", ex.toString());
            }
        };
    }
}
