package com.a2a.kafka.core.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configuration properties for OpenAI integration.
 * Provides validation and fail-fast behavior for OpenAI configuration.
 */
@ConfigurationProperties(prefix = "openai")
@Validated
public class OpenAIProperties {

    @NotBlank(message = "OpenAI API key cannot be blank")
    private String apiKey;

    @NotBlank(message = "OpenAI model cannot be blank")
    private String model = "gpt-3.5-turbo";

    @DecimalMin(value = "0.0", message = "Temperature must be between 0.0 and 2.0")
    @DecimalMax(value = "2.0", message = "Temperature must be between 0.0 and 2.0")
    private double temperature = 0.7;

    @Min(value = 1, message = "Max tokens must be at least 1")
    @Max(value = 4096, message = "Max tokens cannot exceed 4096")
    private int maxTokens = 1000;

    private Duration timeout = Duration.ofSeconds(30);

    @Min(value = 0, message = "Max retries cannot be negative")
    @Max(value = 10, message = "Max retries cannot exceed 10")
    private int maxRetries = 3;

    // Getters and setters
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
}