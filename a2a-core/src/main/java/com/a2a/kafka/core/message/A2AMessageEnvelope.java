package com.a2a.kafka.core.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * A2A Message Envelope structure for standardized agent communication.
 * Based on the A2A protocol specification for message exchange.
 */
public class A2AMessageEnvelope {

    @NotBlank(message = "Message ID cannot be blank")
    @JsonProperty("id")
    private String id;

    @NotNull(message = "Message type cannot be null")
    @JsonProperty("type")
    private MessageType type;

    @NotBlank(message = "From field cannot be blank")
    @JsonProperty("from")
    private String from;

    @JsonProperty("to")
    private String to;

    @JsonProperty("taskId")
    private String taskId;

    @JsonProperty("method")
    private String method;

    @NotNull(message = "Payload cannot be null")
    @JsonProperty("payload")
    private Object payload;

    @NotNull(message = "Timestamp cannot be null")
    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("headers")
    private Map<String, String> headers;

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("replyTo")
    private String replyTo;

    @JsonProperty("ttl")
    private Long ttl;

    // Default constructor
    public A2AMessageEnvelope() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
    }

    // Constructor with required fields
    public A2AMessageEnvelope(MessageType type, String from, Object payload) {
        this();
        this.type = type;
        this.from = from;
        this.payload = payload;
    }

    // Constructor with task ID
    public A2AMessageEnvelope(MessageType type, String from, String taskId, Object payload) {
        this(type, from, payload);
        this.taskId = taskId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    // Utility methods
    public boolean isCommand() {
        return MessageType.COMMAND.equals(this.type);
    }

    public boolean isReply() {
        return MessageType.REPLY.equals(this.type);
    }

    public boolean isEvent() {
        return MessageType.EVENT.equals(this.type);
    }

    public boolean hasTaskId() {
        return this.taskId != null && !this.taskId.trim().isEmpty();
    }

    public boolean isExpired() {
        if (this.ttl == null) {
            return false;
        }
        return Instant.now().isAfter(this.timestamp.plusMillis(this.ttl));
    }

    @Override
    public String toString() {
        return "A2AMessageEnvelope{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", taskId='" + taskId + '\'' +
                ", method='" + method + '\'' +
                ", timestamp=" + timestamp +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        A2AMessageEnvelope that = (A2AMessageEnvelope) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}