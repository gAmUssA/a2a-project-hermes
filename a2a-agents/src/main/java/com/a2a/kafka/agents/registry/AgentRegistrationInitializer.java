package com.a2a.kafka.agents.registry;

import com.a2a.kafka.agents.api.Agent;
import com.a2a.kafka.agents.gp.GeneralPurposeAgent;
import com.a2a.kafka.agents.summarizer.SummarizerAgent;
import com.a2a.kafka.agents.translator.TranslatorAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Automatically registers all Agent beans in the Agent Registry on application startup.
 */
@Component
public class AgentRegistrationInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(AgentRegistrationInitializer.class);

    private final List<Agent> agents;
    private final AgentRegistryService registryService;
    private final AgentRegistryRepository repository;
    private final AtomicBoolean done = new AtomicBoolean(false);

    public AgentRegistrationInitializer(List<Agent> agents, AgentRegistryService registryService, AgentRegistryRepository repository) {
        this.agents = agents;
        this.registryService = registryService;
        this.repository = repository;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Ensure we only run once
        if (done.compareAndSet(false, true)) {
            try {
                registerAllAgents();
            } catch (Exception ex) {
                log.warn("Agent registration on startup failed: {}", ex.toString(), ex);
            }
        }
    }

    /**
     * Exposed for testing. Registers all known agents with their discovered capabilities.
     */
    public void registerAllAgents() {
        for (Agent agent : agents) {
            try {
                Map<String, Object> capabilities = discoverCapabilities(agent);
                Map<String, Object> agentCard = buildAgentCard(agent, capabilities);
                // Publish via messaging (both legacy capabilities and new AgentCard)
                registryService.updateCapabilities(agent.getAgentName(), capabilities);
                registryService.updateAgentCard(agent.getAgentName(), agentCard);
                // Also upsert local repository so UI/queries work even without Kafka
                Map<String, Object> md = new HashMap<>();
                md.put("capabilities", capabilities);
                md.put("agentCard", agentCard);
                repository.upsert(new AgentRegistryEntry(agent.getAgentName(), md, Instant.now()));
                log.info("Registered agent '{}' with capabilities {} and AgentCard", agent.getAgentName(), capabilities);
            } catch (Exception ex) {
                log.warn("Failed to register agent '{}': {}", agent.getAgentName(), ex.toString());
            }
        }
    }

    private Map<String, Object> discoverCapabilities(Agent agent) {
        Map<String, Object> caps = new HashMap<>();
        // Basic discovery based on agent type; can be extended later
        if (agent instanceof TranslatorAgent) {
            caps.put("translate", true);
        }
        if (agent instanceof SummarizerAgent) {
            caps.put("summarize", true);
        }
        if (agent instanceof GeneralPurposeAgent) {
            caps.put("respond", true);
        }
        // Always include status and type basics
        caps.put("status", agent.getHealth().getStatus().name());
        caps.put("type", agent.getClass().getSimpleName());
        return caps;
    }

    private Map<String, Object> buildAgentCard(Agent agent, Map<String, Object> capabilities) {
        Map<String, Object> card = new HashMap<>();
        String name = agent.getAgentName();
        String type = agent.getClass().getSimpleName();
        boolean streaming = (agent instanceof TranslatorAgent) || (agent instanceof SummarizerAgent);
        // Basic top-level fields
        card.put("name", name);
        card.put("description", type);
        card.put("version", "1.0.0-SNAPSHOT");
        // Capabilities per A2A AgentCard concept (streaming + extensions placeholder)
        Map<String, Object> caps = new HashMap<>();
        caps.put("streaming", streaming);
        caps.put("extensions", List.of());
        card.put("capabilities", caps);
        // Default modes
        card.put("defaultInputModes", List.of("text/plain"));
        card.put("defaultOutputModes", List.of("text/plain"));
        // Skills
        List<Map<String, Object>> skills = new ArrayList<>();
        if (capabilities.getOrDefault("translate", false).equals(true)) {
            skills.add(Map.of("id", "translate", "name", "Translator"));
        }
        if (capabilities.getOrDefault("summarize", false).equals(true)) {
            skills.add(Map.of("id", "summarize", "name", "Summarizer"));
        }
        if (capabilities.getOrDefault("respond", false).equals(true)) {
            skills.add(Map.of("id", "respond", "name", "General Purpose"));
        }
        card.put("skills", skills);
        return card;
    }
}
