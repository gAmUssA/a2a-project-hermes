package com.a2a.kafka.agents.stream;

import com.a2a.kafka.core.builder.A2AMessageBuilder;
import com.a2a.kafka.core.message.A2AMessageEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class ProcessorsConfig {

    private static final Logger log = LoggerFactory.getLogger(ProcessorsConfig.class);

    @Bean
    public Function<A2AMessageEnvelope, A2AMessageEnvelope> processAgentTasks() {
        return input -> {
            try {
                if (input == null) {
                    return null;
                }
                if (input.isCommand()) {
                    // Minimal echo reply preserving original message id as taskId and correlationId
                    return A2AMessageBuilder.reply("processor", input.getId(), A2AMessageBuilder.Payloads.success(input.getPayload()))
                            .correlationId(input.getCorrelationId())
                            .build();
                } else {
                    log.debug("Received non-command message; passing through as event");
                    return A2AMessageBuilder.event("processor", input.getPayload())
                            .correlationId(input.getCorrelationId())
                            .build();
                }
            } catch (Exception ex) {
                log.error("Error processing message {}: {}", input != null ? input.getId() : "null", ex.toString(), ex);
                String taskId = (input != null && input.getId() != null) ? input.getId() : "unknown";
                String correlationId = input != null ? input.getCorrelationId() : null;
                return A2AMessageBuilder.reply("processor", taskId, A2AMessageBuilder.Payloads.error("PROCESSING_ERROR", ex.getMessage()))
                        .correlationId(correlationId)
                        .build();
            }
        };
    }
}
