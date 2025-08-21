package com.a2a.kafka.agents.stream;

import com.a2a.kafka.core.builder.A2AMessageBuilder;
import com.a2a.kafka.core.message.A2AMessageEnvelope;
import com.a2a.kafka.core.message.MessageType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {
        com.a2a.kafka.agents.stream.ProcessorsConfig.class
})
class ProcessorsIntegrationTest {

    @Autowired
    private Function<A2AMessageEnvelope, A2AMessageEnvelope> processAgentTasks;

    @Test
    void commandFlowProducesReply() {
        A2AMessageEnvelope cmd = A2AMessageBuilder
                .command("tester", "agent-x", "do", A2AMessageBuilder.Payloads.text("hello"))
                .correlationId("corr-100")
                .build();
        A2AMessageEnvelope out = processAgentTasks.apply(cmd);
        assertNotNull(out);
        assertEquals("corr-100", out.getCorrelationId());
        assertEquals(com.a2a.kafka.core.message.MessageType.REPLY, out.getType());
    }

    @Test
    void eventFlowPassesThrough() {
        A2AMessageEnvelope evt = A2AMessageBuilder
                .event("tester", A2AMessageBuilder.Payloads.text("world"))
                .correlationId("corr-200")
                .build();
        A2AMessageEnvelope out = processAgentTasks.apply(evt);
        assertNotNull(out);
        assertEquals(com.a2a.kafka.core.message.MessageType.EVENT, out.getType());
        assertEquals("corr-200", out.getCorrelationId());
    }
}
