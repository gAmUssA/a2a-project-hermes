package com.a2a.kafka.agents.config;

import com.a2a.kafka.core.config.A2ASystemProperties;
import org.springframework.stereotype.Service;

@Service
public class AgentConfigurationService {

    private final A2ASystemProperties properties;

    public AgentConfigurationService(A2ASystemProperties properties) {
        this.properties = properties;
    }

    public boolean isTranslatorEnabled() {
        return properties.getAgents().getTranslator().isEnabled();
    }

    public int translatorMaxConcurrent() {
        return properties.getAgents().getTranslator().getMaxConcurrent();
    }

    public boolean isSummarizerEnabled() {
        return properties.getAgents().getSummarizer().isEnabled();
    }

    public int summarizerMaxConcurrent() {
        return properties.getAgents().getSummarizer().getMaxConcurrent();
    }

    public boolean isLlmEnabled() {
        return properties.getAgents().getLlm().isEnabled();
    }

    public int llmMaxConcurrent() {
        return properties.getAgents().getLlm().getMaxConcurrent();
    }
}
