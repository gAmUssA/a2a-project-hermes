package com.a2a.kafka.agents.gp;

import com.a2a.kafka.agents.core.AbstractBaseAgent;
import com.a2a.kafka.agents.service.ChatServiceClient;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class GeneralPurposeAgent extends AbstractBaseAgent {

    private final ChatServiceClient chat;
    private final Set<String> bannedWords;

    public GeneralPurposeAgent(ChatServiceClient chat) {
        super("llm");
        this.chat = Objects.requireNonNull(chat, "chat");
        this.bannedWords = new HashSet<>(Set.of(
                "violence", "hate", "terror", "exploit"
        ));
    }

    @Override
    protected void onStart() {
        // no-op for now
    }

    @Override
    protected void onStop() {
        // no-op for now
    }

    public String respond(String text) {
        String cleaned = sanitize(text);
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        String safe = applySafetyFilter(cleaned);
        return chat.chat("general", Map.of("text", safe));
    }

    public String respond(String text, Integer maxTokensOverride, Double temperatureOverride) {
        String cleaned = sanitize(text);
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        String safe = applySafetyFilter(cleaned);
        return chat.chat("general", Map.of("text", safe), null, temperatureOverride, maxTokensOverride);
    }

    private String sanitize(String text) {
        if (text == null) return "";
        return text.strip().replaceAll("\\s+", " ");
    }

    private String applySafetyFilter(String input) {
        String out = input;
        for (String banned : bannedWords) {
            out = out.replaceAll("(?i)\\b" + java.util.regex.Pattern.quote(banned) + "\\b", "[REDACTED]");
        }
        return out;
    }
}
