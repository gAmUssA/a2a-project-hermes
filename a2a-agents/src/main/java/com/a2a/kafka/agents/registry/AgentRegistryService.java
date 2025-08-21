package com.a2a.kafka.agents.registry;

import com.a2a.kafka.core.builder.A2AMessageBuilder;
import com.a2a.kafka.core.message.A2AMessageEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class AgentRegistryService {

    private static final Logger log = LoggerFactory.getLogger(AgentRegistryService.class);

    private final StreamBridge streamBridge;

    public AgentRegistryService(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishMetadata(String agentName, Map<String, Object> metadata) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("agent", agentName);
        payload.put("metadata", metadata == null ? Map.of() : metadata);
        A2AMessageEnvelope envelope = A2AMessageBuilder
                .event("registry", payload)
                .build();

        Message<A2AMessageEnvelope> message = MessageBuilder.withPayload(envelope)
                .setHeader("contentType", "application/json")
                .setHeader("kafka_messageKey", agentName.getBytes(StandardCharsets.UTF_8))
                .build();
        boolean sent = streamBridge.send("registryUpdates-out-0", message);
        if (!sent) {
            log.warn("Failed to send registry metadata for agent {}", agentName);
        }
    }

    public void updateCapabilities(String agentName, Map<String, Object> capabilities) {
        // For now treat capabilities as metadata key "capabilities"
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("capabilities", capabilities == null ? Map.of() : capabilities);
        publishMetadata(agentName, metadata);
    }
}
