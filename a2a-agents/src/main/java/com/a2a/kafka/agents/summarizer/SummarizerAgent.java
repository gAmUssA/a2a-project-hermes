package com.a2a.kafka.agents.summarizer;

import com.a2a.kafka.agents.core.AbstractBaseAgent;
import com.a2a.kafka.agents.service.ChatServiceClient;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class SummarizerAgent extends AbstractBaseAgent {

    private static final Set<String> ALLOWED_HINTS = Set.of("short", "medium", "long");

    private final ChatServiceClient chat;

    public SummarizerAgent(ChatServiceClient chat) {
        super("summarizer");
        this.chat = Objects.requireNonNull(chat, "chat");
    }

    @Override
    protected void onStart() {
        // no-op; could warm up caches/resources
    }

    @Override
    protected void onStop() {
        // no-op; release resources if any
    }

    public String summarize(String text, String lengthHint) {
        String cleaned = sanitize(text);
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        String hint = normalizeHint(lengthHint);
        if (!ALLOWED_HINTS.contains(hint)) {
            throw new IllegalArgumentException("Invalid length hint: " + lengthHint);
        }
        // Graceful handling of very short content: if extremely short, just return as-is
        if (cleaned.length() < 20) {
            return cleaned;
        }
        Map<String, Object> vars = new HashMap<>();
        vars.put("text", cleaned);
        vars.put("lengthHint", hint);
        return chat.chat("summarizer", vars);
    }

    public Stream<String> summarizeStream(String text, String lengthHint) {
        String summary = summarize(text, lengthHint);
        if (summary.length() <= 60) {
            return Stream.of(summary);
        }
        int chunk = Math.max(30, summary.length() / 3);
        return Stream.iterate(0, i -> i + chunk)
                .limit((summary.length() + chunk - 1) / chunk)
                .map(start -> summary.substring(start, Math.min(start + chunk, summary.length())));
    }

    private String sanitize(String text) {
        if (text == null) return "";
        String s = text.strip().replaceAll("\\s+", " ");
        return s;
    }

    private String normalizeHint(String hint) {
        return hint == null ? "" : hint.toLowerCase(Locale.ROOT).trim();
    }
}
