package com.a2a.kafka.core.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for A2A System settings.
 * Provides validation and fail-fast behavior for system configuration.
 */
@ConfigurationProperties(prefix = "a2a")
@Validated
public class A2ASystemProperties {

    @Valid
    @NotNull
    private SystemInfo system = new SystemInfo();

    @Valid
    @NotNull
    private TopicConfiguration topics = new TopicConfiguration();

    @Valid
    @NotNull
    private AgentConfiguration agents = new AgentConfiguration();

    @Valid
    @NotNull
    private OrchestratorConfiguration orchestrator = new OrchestratorConfiguration();

    // Getters and setters
    public SystemInfo getSystem() {
        return system;
    }

    public void setSystem(SystemInfo system) {
        this.system = system;
    }

    public TopicConfiguration getTopics() {
        return topics;
    }

    public void setTopics(TopicConfiguration topics) {
        this.topics = topics;
    }

    public AgentConfiguration getAgents() {
        return agents;
    }

    public void setAgents(AgentConfiguration agents) {
        this.agents = agents;
    }

    public OrchestratorConfiguration getOrchestrator() {
        return orchestrator;
    }

    public void setOrchestrator(OrchestratorConfiguration orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * System information configuration
     */
    public static class SystemInfo {
        @NotBlank(message = "System name cannot be blank")
        private String name = "A2A Kafka Agent System";

        @NotBlank(message = "System version cannot be blank")
        private String version = "1.0.0-SNAPSHOT";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    /**
     * Kafka topic configuration
     */
    public static class TopicConfiguration {
        @NotBlank(message = "Tasks topic name cannot be blank")
        private String tasks = "a2a.tasks";

        @NotBlank(message = "Replies topic name cannot be blank")
        private String replies = "a2a.replies";

        @NotBlank(message = "Events topic name cannot be blank")
        private String events = "a2a.events";

        @NotBlank(message = "Registry topic name cannot be blank")
        private String registry = "a2a.registry";

        public String getTasks() {
            return tasks;
        }

        public void setTasks(String tasks) {
            this.tasks = tasks;
        }

        public String getReplies() {
            return replies;
        }

        public void setReplies(String replies) {
            this.replies = replies;
        }

        public String getEvents() {
            return events;
        }

        public void setEvents(String events) {
            this.events = events;
        }

        public String getRegistry() {
            return registry;
        }

        public void setRegistry(String registry) {
            this.registry = registry;
        }
    }

    /**
     * Agent configuration
     */
    public static class AgentConfiguration {
        @Valid
        @NotNull
        private AgentSettings translator = new AgentSettings();

        @Valid
        @NotNull
        private AgentSettings summarizer = new AgentSettings();

        @Valid
        @NotNull
        private AgentSettings llm = new AgentSettings();

        public AgentSettings getTranslator() {
            return translator;
        }

        public void setTranslator(AgentSettings translator) {
            this.translator = translator;
        }

        public AgentSettings getSummarizer() {
            return summarizer;
        }

        public void setSummarizer(AgentSettings summarizer) {
            this.summarizer = summarizer;
        }

        public AgentSettings getLlm() {
            return llm;
        }

        public void setLlm(AgentSettings llm) {
            this.llm = llm;
        }
    }

    /**
     * Individual agent settings
     */
    public static class AgentSettings {
        private boolean enabled = true;

        @Min(value = 1, message = "Max concurrent must be at least 1")
        @Max(value = 100, message = "Max concurrent cannot exceed 100")
        private int maxConcurrent = 10;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxConcurrent() {
            return maxConcurrent;
        }

        public void setMaxConcurrent(int maxConcurrent) {
            this.maxConcurrent = maxConcurrent;
        }
    }

    /**
     * Orchestrator configuration
     */
    public static class OrchestratorConfiguration {
        private boolean enabled = true;

        @Min(value = 1, message = "Max chain length must be at least 1")
        @Max(value = 50, message = "Max chain length cannot exceed 50")
        private int maxChainLength = 10;

        @Min(value = 10, message = "Timeout must be at least 10 seconds")
        @Max(value = 3600, message = "Timeout cannot exceed 3600 seconds")
        private int timeoutSeconds = 300;

        @Min(value = 1, message = "Max concurrent workflows must be at least 1")
        @Max(value = 1000, message = "Max concurrent workflows cannot exceed 1000")
        private int maxConcurrentWorkflows = 50;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxChainLength() {
            return maxChainLength;
        }

        public void setMaxChainLength(int maxChainLength) {
            this.maxChainLength = maxChainLength;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public int getMaxConcurrentWorkflows() {
            return maxConcurrentWorkflows;
        }

        public void setMaxConcurrentWorkflows(int maxConcurrentWorkflows) {
            this.maxConcurrentWorkflows = maxConcurrentWorkflows;
        }
    }
}