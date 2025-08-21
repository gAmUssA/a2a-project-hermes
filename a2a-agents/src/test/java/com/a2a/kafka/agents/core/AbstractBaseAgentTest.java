package com.a2a.kafka.agents.core;

import com.a2a.kafka.agents.api.Agent;
import com.a2a.kafka.agents.monitoring.AgentsHealthIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AbstractBaseAgentTest {

    static class DummyAgent extends AbstractBaseAgent {
        private boolean started = false;
        private boolean stopped = false;

        DummyAgent(String name) {
            super(name);
        }

        @Override
        protected void onStart() {
            started = true;
        }

        @Override
        protected void onStop() {
            stopped = true;
        }

        boolean isStartedHookCalled() { return started; }
        boolean isStoppedHookCalled() { return stopped; }
    }

    @Test
    void lifecycleTransitionsAndHealth() {
        DummyAgent agent = new DummyAgent("dummy");
        assertFalse(agent.isRunning());
        assertEquals(AgentHealthStatus.STOPPED, agent.getHealth().getStatus());

        agent.start();
        assertTrue(agent.isRunning());
        assertTrue(agent.isStartedHookCalled());
        assertEquals(AgentHealthStatus.UP, agent.getHealth().getStatus());

        AgentsHealthIndicator indicator = new AgentsHealthIndicator(List.of(agent));
        Health health = indicator.health();
        assertEquals(org.springframework.boot.actuate.health.Status.UP, health.getStatus());

        agent.stop();
        assertFalse(agent.isRunning());
        assertTrue(agent.isStoppedHookCalled());
        assertEquals(AgentHealthStatus.STOPPED, agent.getHealth().getStatus());
    }

    @Test
    void indicatorReportsDownWhenAnyAgentDown() {
        DummyAgent ok = new DummyAgent("ok");
        ok.start();
        Agent failing = new AbstractBaseAgent("failing") {
            @Override
            protected void onStart() {
                throw new RuntimeException("boom");
            }
            @Override
            protected void onStop() {}
        };
        failing.start();

        AgentsHealthIndicator indicator = new AgentsHealthIndicator(List.of(ok, failing));
        Health health = indicator.health();
        assertEquals(org.springframework.boot.actuate.health.Status.DOWN, health.getStatus());
    }
}
