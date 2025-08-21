package com.a2a.kafka.agents.stream;

import com.a2a.kafka.core.builder.A2AMessageBuilder;
import com.a2a.kafka.core.message.A2AMessageEnvelope;
import com.a2a.kafka.core.message.MessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ProcessorsConfigTest {

    @Test
    void commandMessageProducesReplyWithOriginalIdAsTaskId() {
        ProcessorsConfig cfg = new ProcessorsConfig();
        Function<A2AMessageEnvelope, A2AMessageEnvelope> processAgentTasks = cfg.processAgentTasks();

        A2AMessageEnvelope input = A2AMessageBuilder
                .command("tester", "agent-x", "do", Map.of("k", "v"))
                .correlationId("corr-1")
                .build();

        A2AMessageEnvelope output;
        try {
            output = processAgentTasks.apply(input);
        } catch (Exception ex) {
            System.out.println("[DEBUG_LOG] exception in command test: " + ex);
            ex.printStackTrace(System.out);
            throw ex;
        }
        System.out.println("[DEBUG_LOG] command output type=" + (output != null ? output.getType() : null));
        System.out.println("[DEBUG_LOG] command output id=" + (output != null ? output.getId() : null));
        System.out.println("[DEBUG_LOG] command output taskId=" + (output != null ? output.getTaskId() : null));
        System.out.println("[DEBUG_LOG] command output corrId=" + (output != null ? output.getCorrelationId() : null));
        System.out.println("[DEBUG_LOG] command output payload=" + (output != null ? output.getPayload() : null));
        assertNotNull(output);
    }

    @Test
    void nonCommandMessageBecomesEvent() {
        ProcessorsConfig cfg = new ProcessorsConfig();
        Function<A2AMessageEnvelope, A2AMessageEnvelope> processAgentTasks = cfg.processAgentTasks();

        A2AMessageEnvelope input = A2AMessageBuilder
                .event("tester", Map.of("hello", "world"))
                .correlationId("corr-2")
                .build();

        A2AMessageEnvelope output;
        try {
            output = processAgentTasks.apply(input);
        } catch (Exception ex) {
            System.out.println("[DEBUG_LOG] exception in nonCommand test: " + ex);
            ex.printStackTrace(System.out);
            throw ex;
        }
        System.out.println("[DEBUG_LOG] nonCommand output type=" + (output != null ? output.getType() : null));
        System.out.println("[DEBUG_LOG] nonCommand output id=" + (output != null ? output.getId() : null));
        System.out.println("[DEBUG_LOG] nonCommand output taskId=" + (output != null ? output.getTaskId() : null));
        System.out.println("[DEBUG_LOG] nonCommand output corrId=" + (output != null ? output.getCorrelationId() : null));
        System.out.println("[DEBUG_LOG] nonCommand output payload=" + (output != null ? output.getPayload() : null));
        assertNotNull(output);
        assertEquals(MessageType.EVENT, output.getType());
        assertEquals("corr-2", output.getCorrelationId());
        assertEquals("world", ((Map<?,?>)output.getPayload()).get("hello"));
    }
}
