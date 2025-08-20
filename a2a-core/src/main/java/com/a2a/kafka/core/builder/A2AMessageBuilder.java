package com.a2a.kafka.core.builder;

import com.a2a.kafka.core.message.A2AMessageEnvelope;
import com.a2a.kafka.core.message.MessageType;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Builder for creating A2A message envelopes with proper defaults and validation.
 * Provides a fluent API for constructing compliant A2A messages.
 */
public class A2AMessageBuilder {

    private String id;
    private MessageType type;
    private String from;
    private String to;
    private String taskId;
    private String method;
    private Object payload;
    private Instant timestamp;
    private Map<String, String> headers;
    private String correlationId;
    private String replyTo;
    private Long ttl;

    private A2AMessageBuilder() {
        // Initialize with defaults
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.headers = new HashMap<>();
    }

    /**
     * Creates a new builder for command messages.
     * 
     * @param from the sender agent name
     * @param to the target agent name
     * @param method the command method
     * @param payload the command payload
     * @return a new builder instance
     */
    public static A2AMessageBuilder command(String from, String to, String method, Object payload) {
        return new A2AMessageBuilder()
                .type(MessageType.COMMAND)
                .from(from)
                .to(to)
                .method(method)
                .payload(payload);
    }

    /**
     * Creates a new builder for reply messages.
     * 
     * @param from the sender agent name
     * @param taskId the task ID to reply to
     * @param payload the reply payload
     * @return a new builder instance
     */
    public static A2AMessageBuilder reply(String from, String taskId, Object payload) {
        return new A2AMessageBuilder()
                .type(MessageType.REPLY)
                .from(from)
                .taskId(taskId)
                .payload(payload);
    }

    /**
     * Creates a new builder for event messages.
     * 
     * @param from the sender agent name
     * @param payload the event payload
     * @return a new builder instance
     */
    public static A2AMessageBuilder event(String from, Object payload) {
        return new A2AMessageBuilder()
                .type(MessageType.EVENT)
                .from(from)
                .payload(payload);
    }

    /**
     * Creates a new empty builder.
     * 
     * @return a new builder instance
     */
    public static A2AMessageBuilder create() {
        return new A2AMessageBuilder();
    }

    public A2AMessageBuilder id(String id) {
        this.id = id;
        return this;
    }

    public A2AMessageBuilder type(MessageType type) {
        this.type = type;
        return this;
    }

    public A2AMessageBuilder from(String from) {
        this.from = from;
        return this;
    }

    public A2AMessageBuilder to(String to) {
        this.to = to;
        return this;
    }

    public A2AMessageBuilder taskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public A2AMessageBuilder method(String method) {
        this.method = method;
        return this;
    }

    public A2AMessageBuilder payload(Object payload) {
        this.payload = payload;
        return this;
    }

    public A2AMessageBuilder timestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public A2AMessageBuilder header(String key, String value) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.put(key, value);
        return this;
    }

    public A2AMessageBuilder headers(Map<String, String> headers) {
        if (headers != null) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.putAll(headers);
        }
        return this;
    }

    public A2AMessageBuilder correlationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public A2AMessageBuilder replyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public A2AMessageBuilder ttl(Long ttl) {
        this.ttl = ttl;
        return this;
    }

    /**
     * Sets TTL in seconds from now.
     * 
     * @param seconds the TTL in seconds
     * @return this builder
     */
    public A2AMessageBuilder ttlSeconds(long seconds) {
        this.ttl = seconds * 1000L; // Convert to milliseconds
        return this;
    }

    /**
     * Sets TTL in minutes from now.
     * 
     * @param minutes the TTL in minutes
     * @return this builder
     */
    public A2AMessageBuilder ttlMinutes(long minutes) {
        this.ttl = minutes * 60 * 1000L; // Convert to milliseconds
        return this;
    }

    /**
     * Builds the A2A message envelope.
     * 
     * @return the constructed message envelope
     * @throws IllegalStateException if required fields are missing
     */
    public A2AMessageEnvelope build() {
        validateRequiredFields();

        A2AMessageEnvelope envelope = new A2AMessageEnvelope();
        envelope.setId(this.id);
        envelope.setType(this.type);
        envelope.setFrom(this.from);
        envelope.setTo(this.to);
        envelope.setTaskId(this.taskId);
        envelope.setMethod(this.method);
        envelope.setPayload(this.payload);
        envelope.setTimestamp(this.timestamp);
        envelope.setHeaders(this.headers);
        envelope.setCorrelationId(this.correlationId);
        envelope.setReplyTo(this.replyTo);
        envelope.setTtl(this.ttl);

        return envelope;
    }

    private void validateRequiredFields() {
        if (this.type == null) {
            throw new IllegalStateException("Message type is required");
        }

        if (isBlank(this.from)) {
            throw new IllegalStateException("From field is required");
        }

        if (this.payload == null) {
            throw new IllegalStateException("Payload is required");
        }

        // Type-specific validations
        switch (this.type) {
            case COMMAND:
                if (isBlank(this.to)) {
                    throw new IllegalStateException("To field is required for command messages");
                }
                if (isBlank(this.method)) {
                    throw new IllegalStateException("Method is required for command messages");
                }
                break;
            case REPLY:
                if (isBlank(this.taskId)) {
                    throw new IllegalStateException("Task ID is required for reply messages");
                }
                break;
            case EVENT:
                // Events are more flexible, no additional requirements
                break;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Utility class for creating common message payloads.
     */
    public static class Payloads {

        /**
         * Creates a simple text payload.
         */
        public static Map<String, Object> text(String content) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "text");
            payload.put("content", content);
            return payload;
        }

        /**
         * Creates an error payload.
         */
        public static Map<String, Object> error(String code, String message) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "error");
            payload.put("code", code);
            payload.put("message", message);
            return payload;
        }

        /**
         * Creates a success payload.
         */
        public static Map<String, Object> success(Object result) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "success");
            payload.put("result", result);
            return payload;
        }

        /**
         * Creates a translation request payload.
         */
        public static Map<String, Object> translationRequest(String text, String targetLanguage) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "translation_request");
            payload.put("text", text);
            payload.put("target_language", targetLanguage);
            return payload;
        }

        /**
         * Creates a summarization request payload.
         */
        public static Map<String, Object> summarizationRequest(String text, String lengthHint) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "summarization_request");
            payload.put("text", text);
            payload.put("length_hint", lengthHint);
            return payload;
        }
    }
}