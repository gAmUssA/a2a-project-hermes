package com.a2a.kafka.agents.service;

import com.a2a.kafka.agents.core.RetryUtils;
import com.a2a.kafka.core.config.OpenAIProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final PromptTemplateService templates;
    private final OpenAIProperties openAIProperties;

    public ChatService(ChatClient chatClient, PromptTemplateService templates, OpenAIProperties openAIProperties) {
        this.chatClient = chatClient;
        this.templates = templates;
        this.openAIProperties = openAIProperties;
    }

    public String chat(String templateName, Map<String, Object> variables) {
        return chat(templateName, variables, null, null, null);
    }

    public String chat(String templateName, Map<String, Object> variables, String modelOverride, Double temperatureOverride, Integer maxTokensOverride) {
        Objects.requireNonNull(templateName, "templateName");
        String prompt = templates.render(templateName, variables);
        prompt = truncateForTokenBudget(prompt, maxTokensOverride != null ? maxTokensOverride : openAIProperties.getMaxTokens());

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder();
        if (modelOverride != null && !modelOverride.isBlank()) {
            optionsBuilder.model(modelOverride);
        }
        if (temperatureOverride != null) {
            optionsBuilder.temperature(temperatureOverride);
        }
        if (maxTokensOverride != null) {
            optionsBuilder.maxTokens(maxTokensOverride);
        }

        final OpenAiChatOptions options = optionsBuilder.build();
        final String promptInput = prompt;

        int maxAttempts = Math.max(1, openAIProperties.getMaxRetries());
        Duration initialBackoff = Duration.ofMillis(200);
        double multiplier = 2.0;

        return RetryUtils.runWithRetry(() ->
                chatClient
                        .prompt()
                        .user(promptInput)
                        .options(options)
                        .call()
                        .content(),
            maxAttempts,
            initialBackoff,
            multiplier
        );
    }

    private String truncateForTokenBudget(String text, int maxTokens) {
        // Very naive heuristic: assume 4 chars per token; keep some margin for system overhead
        int approxMaxChars = Math.max(256, (int) (maxTokens * 4 * 0.9));
        if (text == null) return null;
        if (text.length() <= approxMaxChars) return text;
        String truncated = text.substring(0, approxMaxChars);
        log.debug("Prompt truncated from {} to {} chars to respect token budget {}", text.length(), truncated.length(), maxTokens);
        return truncated;
    }
}
