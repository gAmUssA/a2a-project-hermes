package com.a2a.kafka.core.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Core configuration class for A2A Kafka Agent System.
 * Enables configuration properties and provides fail-fast validation.
 */
@Configuration
@EnableConfigurationProperties({
    A2ASystemProperties.class,
    OpenAIProperties.class
})
public class A2ACoreConfiguration {
    // Configuration beans will be automatically created by Spring Boot
}