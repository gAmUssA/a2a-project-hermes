package com.a2a.kafka.core.serialization;

import com.a2a.kafka.core.message.A2AMessageEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Kafka deserializer for A2A message envelopes.
 * Converts JSON byte arrays back to A2AMessageEnvelope objects from Kafka transport.
 */
public class A2AMessageDeserializer implements Deserializer<A2AMessageEnvelope> {

    private static final Logger logger = LoggerFactory.getLogger(A2AMessageDeserializer.class);
    
    private final ObjectMapper objectMapper;

    public A2AMessageDeserializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // Configure to read dates as ISO strings
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No additional configuration needed
    }

    @Override
    public A2AMessageEnvelope deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            logger.debug("Null or empty data received for deserialization on topic: {}", topic);
            return null;
        }

        try {
            A2AMessageEnvelope envelope = objectMapper.readValue(data, A2AMessageEnvelope.class);
            logger.debug("Deserialized A2A message envelope with ID: {} from topic: {}", 
                    envelope.getId(), topic);
            return envelope;
            
        } catch (IOException e) {
            String errorMsg = String.format("Failed to deserialize A2A message envelope from topic: %s. " +
                    "Data length: %d bytes", topic, data.length);
            logger.error(errorMsg, e);
            
            // Log the raw data for debugging (first 100 bytes only)
            if (logger.isDebugEnabled()) {
                String rawData = new String(data, 0, Math.min(data.length, 100));
                logger.debug("Raw data (first 100 bytes): {}", rawData);
            }
            
            throw new SerializationException(errorMsg, e);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }
}