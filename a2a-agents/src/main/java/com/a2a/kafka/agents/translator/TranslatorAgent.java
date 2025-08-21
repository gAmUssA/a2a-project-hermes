package com.a2a.kafka.agents.translator;

import com.a2a.kafka.agents.core.AbstractBaseAgent;
import com.a2a.kafka.agents.service.ChatServiceClient;
import com.a2a.kafka.agents.util.LanguageDetector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class TranslatorAgent extends AbstractBaseAgent {

    private final ChatServiceClient chat;
    private final LanguageDetector detector;

    public TranslatorAgent(ChatServiceClient chat, LanguageDetector detector) {
        super("translator");
        this.chat = Objects.requireNonNull(chat, "chat");
        this.detector = Objects.requireNonNull(detector, "detector");
    }

    @Override
    protected void onStart() {
        // no-op for now; initialize resources if needed later
    }

    @Override
    protected void onStop() {
        // no-op for now; cleanup resources if needed later
    }

    public TranslationResult translate(String text, String targetLanguage) {
        String sanitized = sanitize(text);
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        String target = normalizeLang(targetLanguage);
        if (!detector.isSupported(target)) {
            throw new IllegalArgumentException("Unsupported target language: " + targetLanguage);
        }
        String source = detector.detect(sanitized);
        double confidence = "unknown".equals(source) ? 0.5 : 0.9;

        Map<String, Object> vars = new HashMap<>();
        vars.put("text", sanitized);
        vars.put("targetLanguage", target);
        String translated = chat.chat("translator", vars);
        return new TranslationResult(translated, source, target, confidence);
    }

    public Stream<String> translateStream(String text, String targetLanguage) {
        TranslationResult result = translate(text, targetLanguage);
        String t = result.getTranslatedText();
        if (t.length() <= 40) {
            return Stream.of(t);
        }
        int mid = t.length() / 2;
        return Stream.of(t.substring(0, mid), t.substring(mid));
    }

    private String sanitize(String text) {
        if (text == null) return "";
        // collapse whitespace and trim
        String s = text.strip().replaceAll("\\s+", " ");
        // remove control characters except standard whitespace
        return s.chars()
                .filter(ch -> ch == '\n' || ch == '\t' || ch == ' ' || !Character.isISOControl(ch))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private String normalizeLang(String lang) {
        return lang == null ? "" : lang.trim().toLowerCase();
    }
}
