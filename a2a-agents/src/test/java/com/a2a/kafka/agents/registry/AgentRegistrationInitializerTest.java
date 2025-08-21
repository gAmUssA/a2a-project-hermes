package com.a2a.kafka.agents.registry;

import com.a2a.kafka.agents.api.Agent;
import com.a2a.kafka.agents.gp.GeneralPurposeAgent;
import com.a2a.kafka.agents.service.ChatServiceClient;
import com.a2a.kafka.agents.summarizer.SummarizerAgent;
import com.a2a.kafka.agents.translator.TranslatorAgent;
import com.a2a.kafka.agents.util.LanguageDetector;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AgentRegistrationInitializerTest {

    static class StubChat implements ChatServiceClient {
        @Override
        public String chat(String templateName, Map<String, Object> variables) {
            return "ok";
        }
        @Override
        public String chat(String templateName, Map<String, Object> variables, String modelOverride, Double temperatureOverride, Integer maxTokensOverride) {
            return chat(templateName, variables);
        }
    }

    @Test
    void registersAllAgentsWithCapabilitiesAndAgentCard() {
        ChatServiceClient chat = new StubChat();
        TranslatorAgent translator = new TranslatorAgent(chat, new LanguageDetector());
        SummarizerAgent summarizer = new SummarizerAgent(chat);
        GeneralPurposeAgent gp = new GeneralPurposeAgent(chat);

        AgentRegistryService registryService = mock(AgentRegistryService.class);
        AgentRegistryRepository repository = mock(AgentRegistryRepository.class);
        List<Agent> agents = Arrays.<Agent>asList(translator, summarizer, gp);
        AgentRegistrationInitializer init = new AgentRegistrationInitializer(
                agents, registryService, repository
        );

        init.registerAllAgents();

        // Verify capabilities calls
        ArgumentCaptor<Map<String, Object>> capsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(registryService, times(1)).updateCapabilities(eq("translator"), capsCaptor.capture());
        Map<String, Object> tCaps = capsCaptor.getValue();
        assertTrue((Boolean) tCaps.get("translate"));
        assertEquals("TranslatorAgent", tCaps.get("type"));
        assertNotNull(tCaps.get("status"));

        capsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(registryService, times(1)).updateCapabilities(eq("summarizer"), capsCaptor.capture());
        Map<String, Object> sCaps = capsCaptor.getValue();
        assertTrue((Boolean) sCaps.get("summarize"));
        assertEquals("SummarizerAgent", sCaps.get("type"));

        capsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(registryService, times(1)).updateCapabilities(eq("llm"), capsCaptor.capture());
        Map<String, Object> gCaps = capsCaptor.getValue();
        assertTrue((Boolean) gCaps.get("respond"));
        assertEquals("GeneralPurposeAgent", gCaps.get("type"));

        // Verify AgentCard calls
        ArgumentCaptor<Map<String, Object>> cardCaptor = ArgumentCaptor.forClass(Map.class);
        verify(registryService, times(1)).updateAgentCard(eq("translator"), cardCaptor.capture());
        Map<String, Object> tCard = cardCaptor.getValue();
        assertEquals("translator", tCard.get("name"));
        assertTrue(((Map<?,?>)tCard.get("capabilities")).containsKey("streaming"));

        cardCaptor = ArgumentCaptor.forClass(Map.class);
        verify(registryService, times(1)).updateAgentCard(eq("summarizer"), cardCaptor.capture());
        Map<String, Object> sCard = cardCaptor.getValue();
        assertEquals("summarizer", sCard.get("name"));
        assertTrue(((Map<?,?>)sCard.get("capabilities")).containsKey("streaming"));

        cardCaptor = ArgumentCaptor.forClass(Map.class);
        verify(registryService, times(1)).updateAgentCard(eq("llm"), cardCaptor.capture());
        Map<String, Object> gCard = cardCaptor.getValue();
        assertEquals("llm", gCard.get("name"));
        assertTrue(((Map<?,?>)gCard.get("capabilities")).containsKey("streaming"));

        verifyNoMoreInteractions(registryService);
    }
}
