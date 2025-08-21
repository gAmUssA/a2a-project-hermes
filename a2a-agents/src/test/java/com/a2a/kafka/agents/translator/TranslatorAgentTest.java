package com.a2a.kafka.agents.translator;

import com.a2a.kafka.agents.service.ChatServiceClient;
import com.a2a.kafka.agents.util.LanguageDetector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TranslatorAgentTest {

    static class StubChat implements ChatServiceClient {
        String lastTemplate;
        Map<String, Object> lastVars;
        String forcedReturn;

        @Override
        public String chat(String templateName, Map<String, Object> variables) {
            lastTemplate = templateName;
            lastVars = variables;
            if (forcedReturn != null) return forcedReturn;
            Object t = variables.get("text");
            Object lang = variables.get("targetLanguage");
            return "[" + lang + "] " + t;
        }

        @Override
        public String chat(String templateName, Map<String, Object> variables, String modelOverride, Double temperatureOverride, Integer maxTokensOverride) {
            return chat(templateName, variables);
        }
    }

    @Test
    void translateReturnsMetadataAndUsesTemplate() {
        StubChat stub = new StubChat();
        TranslatorAgent agent = new TranslatorAgent(stub, new LanguageDetector());

        TranslationResult res = agent.translate("Hello world", "es");
        assertNotNull(res);
        assertEquals("es", res.getTargetLanguage());
        assertTrue(res.getConfidence() > 0);
        assertEquals("translator", stub.lastTemplate);
        assertEquals("es", stub.lastVars.get("targetLanguage"));
        assertEquals("[es] Hello world", res.getTranslatedText());
    }

    @Test
    void unsupportedLanguageThrows() {
        StubChat stub = new StubChat();
        TranslatorAgent agent = new TranslatorAgent(stub, new LanguageDetector());
        assertThrows(IllegalArgumentException.class, () -> agent.translate("Hello", "xx"));
    }

    @Test
    void blankInputThrows() {
        StubChat stub = new StubChat();
        TranslatorAgent agent = new TranslatorAgent(stub, new LanguageDetector());
        assertThrows(IllegalArgumentException.class, () -> agent.translate("   \n\t  ", "en"));
    }

    @Test
    void detectsRussianSource() {
        StubChat stub = new StubChat();
        TranslatorAgent agent = new TranslatorAgent(stub, new LanguageDetector());
        TranslationResult res = agent.translate("Привет мир", "en");
        assertEquals("ru", res.getSourceLanguage());
        assertTrue(res.getConfidence() >= 0.8);
    }

    @Test
    void streamingSplitsLongText() {
        StubChat stub = new StubChat();
        TranslatorAgent agent = new TranslatorAgent(stub, new LanguageDetector());
        String longText = "x".repeat(120);
        // Stub returns translated as [en] + text to ensure deterministic size
        stub.forcedReturn = "[en] " + longText;
        List<String> chunks = agent.translateStream(longText, "en").collect(Collectors.toList());
        assertTrue(chunks.size() >= 1);
        String joined = String.join("", chunks);
        assertEquals(stub.forcedReturn, joined);
    }
}
