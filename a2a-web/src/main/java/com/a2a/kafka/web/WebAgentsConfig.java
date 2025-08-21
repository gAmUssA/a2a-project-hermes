package com.a2a.kafka.web;

import com.a2a.kafka.agents.gp.GeneralPurposeAgent;
import com.a2a.kafka.agents.service.ChatServiceClient;
import com.a2a.kafka.agents.summarizer.SummarizerAgent;
import com.a2a.kafka.agents.translator.TranslatorAgent;
import com.a2a.kafka.agents.util.LanguageDetector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebAgentsConfig {

    @Bean
    public LanguageDetector languageDetector() {
        return new LanguageDetector();
    }

    @Bean
    public TranslatorAgent translatorAgent(ChatServiceClient chatServiceClient, LanguageDetector languageDetector) {
        return new TranslatorAgent(chatServiceClient, languageDetector);
    }

    @Bean
    public SummarizerAgent summarizerAgent(ChatServiceClient chatServiceClient) {
        return new SummarizerAgent(chatServiceClient);
    }

    @Bean
    public GeneralPurposeAgent generalPurposeAgent(ChatServiceClient chatServiceClient) {
        return new GeneralPurposeAgent(chatServiceClient);
    }
}
