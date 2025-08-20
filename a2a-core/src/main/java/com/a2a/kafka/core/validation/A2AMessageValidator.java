package com.a2a.kafka.core.validation;

import com.a2a.kafka.core.message.A2AMessageEnvelope;
import com.a2a.kafka.core.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator for A2A message protocol compliance.
 * Ensures messages conform to the A2A protocol specification.
 */
@Component
public class A2AMessageValidator {

    private static final Logger logger = LoggerFactory.getLogger(A2AMessageValidator.class);

    /**
     * Validates an A2A message envelope for protocol compliance.
     * 
     * @param envelope the message envelope to validate
     * @return validation result containing any errors found
     */
    public ValidationResult validate(A2AMessageEnvelope envelope) {
        if (envelope == null) {
            return ValidationResult.invalid("Message envelope cannot be null");
        }

        List<String> errors = new ArrayList<>();

        // Validate required fields
        validateRequiredFields(envelope, errors);
        
        // Validate field formats and constraints
        validateFieldFormats(envelope, errors);
        
        // Validate message type specific rules
        validateMessageTypeRules(envelope, errors);
        
        // Validate timestamps
        validateTimestamps(envelope, errors);

        if (errors.isEmpty()) {
            logger.debug("A2A message envelope validation passed for ID: {}", envelope.getId());
            return ValidationResult.valid();
        } else {
            logger.warn("A2A message envelope validation failed for ID: {} with {} errors", 
                    envelope.getId(), errors.size());
            return ValidationResult.invalid(errors);
        }
    }

    private void validateRequiredFields(A2AMessageEnvelope envelope, List<String> errors) {
        if (isBlank(envelope.getId())) {
            errors.add("Message ID is required and cannot be blank");
        }

        if (envelope.getType() == null) {
            errors.add("Message type is required");
        }

        if (isBlank(envelope.getFrom())) {
            errors.add("From field is required and cannot be blank");
        }

        if (envelope.getPayload() == null) {
            errors.add("Payload is required");
        }

        if (envelope.getTimestamp() == null) {
            errors.add("Timestamp is required");
        }
    }

    private void validateFieldFormats(A2AMessageEnvelope envelope, List<String> errors) {
        // Validate ID format (should be a valid UUID or similar identifier)
        if (envelope.getId() != null && envelope.getId().trim().length() < 3) {
            errors.add("Message ID must be at least 3 characters long");
        }

        // Validate agent names (from/to fields)
        if (envelope.getFrom() != null && !isValidAgentName(envelope.getFrom())) {
            errors.add("From field contains invalid characters");
        }

        if (envelope.getTo() != null && !isValidAgentName(envelope.getTo())) {
            errors.add("To field contains invalid characters");
        }

        // Validate task ID format if present
        if (envelope.getTaskId() != null && envelope.getTaskId().trim().length() < 3) {
            errors.add("Task ID must be at least 3 characters long when provided");
        }
    }

    private void validateMessageTypeRules(A2AMessageEnvelope envelope, List<String> errors) {
        if (envelope.getType() == null) {
            return; // Already handled in required fields validation
        }

        switch (envelope.getType()) {
            case COMMAND:
                validateCommandMessage(envelope, errors);
                break;
            case REPLY:
                validateReplyMessage(envelope, errors);
                break;
            case EVENT:
                validateEventMessage(envelope, errors);
                break;
        }
    }

    private void validateCommandMessage(A2AMessageEnvelope envelope, List<String> errors) {
        // Commands should have a method specified
        if (isBlank(envelope.getMethod())) {
            errors.add("Command messages must specify a method");
        }

        // Commands should have a target agent (to field)
        if (isBlank(envelope.getTo())) {
            errors.add("Command messages must specify a target agent (to field)");
        }
    }

    private void validateReplyMessage(A2AMessageEnvelope envelope, List<String> errors) {
        // Replies should have a task ID to correlate with the original command
        if (isBlank(envelope.getTaskId())) {
            errors.add("Reply messages should have a task ID for correlation");
        }
    }

    private void validateEventMessage(A2AMessageEnvelope envelope, List<String> errors) {
        // Events can be more flexible, but should have meaningful payload
        if (envelope.getPayload() instanceof String && 
            ((String) envelope.getPayload()).trim().isEmpty()) {
            errors.add("Event messages should have meaningful payload content");
        }
    }

    private void validateTimestamps(A2AMessageEnvelope envelope, List<String> errors) {
        if (envelope.getTimestamp() == null) {
            return; // Already handled in required fields validation
        }

        Instant now = Instant.now();
        Instant messageTime = envelope.getTimestamp();

        // Check if timestamp is too far in the future (more than 1 hour)
        if (messageTime.isAfter(now.plusSeconds(3600))) {
            errors.add("Message timestamp is too far in the future");
        }

        // Check if timestamp is too old (more than 24 hours)
        if (messageTime.isBefore(now.minusSeconds(86400))) {
            errors.add("Message timestamp is too old (more than 24 hours)");
        }

        // Check TTL if specified
        if (envelope.getTtl() != null && envelope.isExpired()) {
            errors.add("Message has expired according to its TTL");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isValidAgentName(String agentName) {
        if (isBlank(agentName)) {
            return false;
        }
        
        // Agent names should contain only alphanumeric characters, hyphens, underscores, and dots
        return agentName.matches("^[a-zA-Z0-9._-]+$");
    }

    /**
     * Validation result containing the outcome and any error messages.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        private ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String error) {
            List<String> errors = new ArrayList<>();
            errors.add(error);
            return new ValidationResult(false, errors);
        }

        public static ValidationResult invalid(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public String getErrorsAsString() {
            return String.join("; ", errors);
        }

        @Override
        public String toString() {
            return "ValidationResult{" +
                    "valid=" + valid +
                    ", errors=" + errors +
                    '}';
        }
    }
}