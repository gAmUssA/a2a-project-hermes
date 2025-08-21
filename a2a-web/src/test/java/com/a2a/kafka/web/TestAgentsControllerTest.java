package com.a2a.kafka.web;

import com.a2a.kafka.agents.gp.GeneralPurposeAgent;
import com.a2a.kafka.agents.summarizer.SummarizerAgent;
import com.a2a.kafka.agents.translator.TranslationResult;
import com.a2a.kafka.agents.translator.TranslatorAgent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = TestAgentsController.class)
@Import(TestAgentsControllerTest.MockedAgentsConfig.class)
@TestPropertySource(properties = "spring.webflux.base-path=")
class TestAgentsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TranslatorAgent translatorAgent;

    @Autowired
    private SummarizerAgent summarizerAgent;

    @Autowired
    private GeneralPurposeAgent generalPurposeAgent;

    @TestConfiguration
    static class MockedAgentsConfig {
        @Bean
        TranslatorAgent translatorAgent() { return Mockito.mock(TranslatorAgent.class); }
        @Bean
        SummarizerAgent summarizerAgent() { return Mockito.mock(SummarizerAgent.class); }
        @Bean
        GeneralPurposeAgent generalPurposeAgent() { return Mockito.mock(GeneralPurposeAgent.class); }
    }

    @Test
    void translateEndpointReturnsTranslationResult() {
        when(translatorAgent.translate(eq("Hello"), eq("es")))
                .thenReturn(new TranslationResult("[es] Hello", "en", "es", 0.9));

        webTestClient.post()
                .uri("/test/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"text\":\"Hello\",\"targetLanguage\":\"es\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.translatedText").isEqualTo("[es] Hello")
                .jsonPath("$.sourceLanguage").isEqualTo("en")
                .jsonPath("$.targetLanguage").isEqualTo("es");
    }

    @Test
    void summarizeEndpointReturnsSummary() {
        when(summarizerAgent.summarize(eq("Long content"), eq("short")))
                .thenReturn("SUMMARY(short):Long content");

        webTestClient.post()
                .uri("/test/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"text\":\"Long content\",\"lengthHint\":\"short\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.summary").isEqualTo("SUMMARY(short):Long content");
    }

    @Test
    void gpEndpointReturnsResponseAndHonorsOverrides() {
        when(generalPurposeAgent.respond(eq("Hello GP"), eq(256), eq(0.1)))
                .thenReturn("RESP:Hello GP");

        webTestClient.post()
                .uri("/test/gp")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"text\":\"Hello GP\",\"maxTokens\":256,\"temperature\":0.1}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.response").isEqualTo("RESP:Hello GP");

        verify(generalPurposeAgent).respond("Hello GP", 256, 0.1);
    }
}
