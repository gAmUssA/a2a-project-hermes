package com.a2a.kafka.agents.registry;

import com.a2a.kafka.core.builder.A2AMessageBuilder;
import com.a2a.kafka.core.message.A2AMessageEnvelope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {
        RegistryProcessorsConfig.class,
        AgentRegistryRepository.class
})
class AgentRegistryIntegrationTest {

    @Autowired
    private java.util.function.Consumer<A2AMessageEnvelope> registryUpdates;
    @Autowired
    private AgentRegistryRepository repository;

    @Test
    void registryConsumerUpdatesRepository() {
        Map<String, Object> pl = Map.of(
                "agent", "agent-alpha",
                "metadata", Map.of("capabilities", Map.of("translate", true))
        );
        A2AMessageEnvelope env = A2AMessageBuilder.event("test", pl).build();
        registryUpdates.accept(env);

        assertNotNull(repository.findByName("agent-alpha"));
        assertTrue(((Map<?,?>)repository.findByName("agent-alpha").getMetadata().get("capabilities")).containsKey("translate"));
    }
}
