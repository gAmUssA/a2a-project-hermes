package com.a2a.kafka.core.message;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of A2A message types according to the A2A protocol specification.
 */
public enum MessageType {
    
    /**
     * Command message - represents a request for an agent to perform an action
     */
    COMMAND("command"),
    
    /**
     * Reply message - represents a response to a command
     */
    REPLY("reply"),
    
    /**
     * Event message - represents a notification or status update
     */
    EVENT("event");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Parse a string value to MessageType enum
     * @param value the string value
     * @return the corresponding MessageType
     * @throws IllegalArgumentException if the value is not recognized
     */
    public static MessageType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("MessageType value cannot be null");
        }
        
        for (MessageType type : MessageType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("Unknown MessageType value: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}