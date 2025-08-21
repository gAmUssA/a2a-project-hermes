package com.a2a.kafka.agents.service;

import java.util.Map;

public interface ChatServiceClient {
    String chat(String templateName, Map<String, Object> variables);

    String chat(String templateName, Map<String, Object> variables,
                String modelOverride, Double temperatureOverride, Integer maxTokensOverride);
}
