package com.a2a.kafka.agents.util;

import java.util.Set;

public class LanguageDetector {

    private static final Set<String> SUPPORTED = Set.of("en", "es", "de", "fr", "ru", "zh");

    public String detect(String text) {
        if (text == null || text.isBlank()) {
            return "unknown";
        }
        // Simple heuristic: presence of Cyrillic => ru; otherwise default en
        if (text.codePoints().anyMatch(cp -> Character.UnicodeBlock.of(cp) == Character.UnicodeBlock.CYRILLIC)) {
            return "ru";
        }
        return "en";
    }

    public boolean isSupported(String lang) {
        return SUPPORTED.contains(lang);
    }

    public Set<String> supportedLanguages() {
        return SUPPORTED;
    }
}
