package com.a2a.kafka.agents.gp;

import com.a2a.kafka.agents.service.ChatServiceClient;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GeneralPurposeAgentTest {

    static class StubChat implements ChatServiceClient {
        String lastTemplate;
        Map<String, Object> lastVars;
        String forcedReturn;
        Integer lastMaxTokens;
        Double lastTemperature;
        @Override
        public String chat(String templateName, Map<String, Object> variables) {
            lastTemplate = templateName;
            lastVars = variables;
            if (forcedReturn != null) return forcedReturn;
            return "RESP:" + variables.get("text");
        }
        @Override
        public String chat(String templateName, Map<String, Object> variables, String modelOverride, Double temperatureOverride, Integer maxTokensOverride) {
            lastTemplate = templateName;
            lastVars = variables;
            lastTemperature = temperatureOverride;
            lastMaxTokens = maxTokensOverride;
            return chat(templateName, variables);
        }
    }

    @Test
    void safetyFilterRedactsBannedWords() {
        StubChat stub = new StubChat();
        GeneralPurposeAgent agent = new GeneralPurposeAgent(stub);
        String resp = agent.respond("This contains violence and HATE speech.");
        assertEquals("general", stub.lastTemplate);
        String sent = (String) stub.lastVars.get("text");
        assertTrue(sent.contains("[REDACTED]"));
        assertFalse(sent.toLowerCase().contains("violence"));
        assertFalse(sent.toLowerCase().contains("hate"));
    }

    @Test
    void overridesArePassed() {
        StubChat stub = new StubChat();
        GeneralPurposeAgent agent = new GeneralPurposeAgent(stub);
        String resp = agent.respond("hello world", 256, 0.1);
        assertEquals(256, stub.lastMaxTokens);
        assertEquals(0.1, stub.lastTemperature);
    }

    @Test
    void blankInputThrows() {
        StubChat stub = new StubChat();
        GeneralPurposeAgent agent = new GeneralPurposeAgent(stub);
        assertThrows(IllegalArgumentException.class, () -> agent.respond("   \n\t  "));
    }
}
