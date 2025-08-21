package com.a2a.kafka.agents.summarizer;

import com.a2a.kafka.agents.service.ChatServiceClient;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SummarizerAgentTest {

    static class StubChat implements ChatServiceClient {
        String lastTemplate;
        Map<String, Object> lastVars;
        String forcedReturn;
        @Override
        public String chat(String templateName, Map<String, Object> variables) {
            lastTemplate = templateName;
            lastVars = variables;
            if (forcedReturn != null) return forcedReturn;
            return "SUMMARY(" + variables.get("lengthHint") + "):" + variables.get("text");
        }
        @Override
        public String chat(String templateName, Map<String, Object> variables, String modelOverride, Double temperatureOverride, Integer maxTokensOverride) {
            return chat(templateName, variables);
        }
    }

    @Test
    void validatesLengthHintAndUsesTemplate() {
        StubChat stub = new StubChat();
        SummarizerAgent agent = new SummarizerAgent(stub);
        String res = agent.summarize("This is a fairly long content that should be summarized by the stub.", "short");
        assertNotNull(res);
        assertEquals("summarizer", stub.lastTemplate);
        assertEquals("short", stub.lastVars.get("lengthHint"));
    }

    @Test
    void invalidHintThrows() {
        StubChat stub = new StubChat();
        SummarizerAgent agent = new SummarizerAgent(stub);
        assertThrows(IllegalArgumentException.class, () -> agent.summarize("Hello world", "tiny"));
    }

    @Test
    void shortContentReturnsAsIs() {
        StubChat stub = new StubChat();
        SummarizerAgent agent = new SummarizerAgent(stub);
        String shortText = "Short content.";
        String res = agent.summarize(shortText, "short");
        assertEquals(shortText, res);
    }

    @Test
    void streamingSplitsLongSummary() {
        StubChat stub = new StubChat();
        SummarizerAgent agent = new SummarizerAgent(stub);
        stub.forcedReturn = "X".repeat(200);
        var chunks = agent.summarizeStream("Some long input to be summarized into a long output.", "long").collect(Collectors.toList());
        assertTrue(chunks.size() >= 3);
        String rejoined = String.join("", chunks);
        assertEquals(stub.forcedReturn, rejoined);
    }

    @Test
    void blankInputThrows() {
        StubChat stub = new StubChat();
        SummarizerAgent agent = new SummarizerAgent(stub);
        assertThrows(IllegalArgumentException.class, () -> agent.summarize("   \t\n  ", "short"));
    }
}
