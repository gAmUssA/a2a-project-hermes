package com.a2a.kafka.core.serialization;

import com.a2a.kafka.core.message.A2AMessageEnvelope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Kafka serializer for A2A message envelopes.
 * Converts A2AMessageEnvelope objects to JSON byte arrays for Kafka transport.
 */
public class A2AMessageSerializer implements Serializer<A2AMessageEnvelope> {

    private static final Logger logger = LoggerFactory.getLogger(A2AMessageSerializer.class);
    
    private final ObjectMapper objectMapper;

    public A2AMessageSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // Configure to write dates as ISO strings
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No additional configuration needed
    }

    @Override
    public byte[] serialize(String topic, A2AMessageEnvelope data) {
        if (data == null) {
            logger.debug("Null data received for serialization on topic: {}", topic);
            return null;
        }

        try {
            byte[] result = objectMapper.writeValueAsBytes(data);
            logger.debug("Serialized A2A message envelope with ID: {} for topic: {}", data.getId(), topic);
            return result;
            
        } catch (JsonProcessingException e) {
            String errorMsg = String.format("Failed to serialize A2A message envelope with ID: %s for topic: %s", 
                    data.getId(), topic);
            logger.error(errorMsg, e);
            throw new SerializationException(errorMsg, e);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }
}