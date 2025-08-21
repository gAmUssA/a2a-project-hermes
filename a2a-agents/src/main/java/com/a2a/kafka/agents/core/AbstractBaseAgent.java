package com.a2a.kafka.agents.core;

import com.a2a.kafka.agents.api.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractBaseAgent implements Agent, SmartLifecycle {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final String agentName;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<AgentHealth> health = new AtomicReference<>(
            new AgentHealth(AgentHealthStatus.STOPPED, Collections.emptyMap(), null, Instant.now())
    );

    protected AbstractBaseAgent(String agentName) {
        this.agentName = agentName;
    }

    @Override
    public String getAgentName() {
        return agentName;
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            logger.info("Starting agent: {}", agentName);
            health.set(new AgentHealth(AgentHealthStatus.STARTING));
            try {
                onStart();
                health.set(new AgentHealth(AgentHealthStatus.UP));
                logger.info("Agent started: {}", agentName);
                Map<String, Object> details = new HashMap<>();
                details.put("event", "start");
                AuditLogger.log("agent_started", agentName, details);
            } catch (Exception ex) {
                running.set(false);
                health.set(new AgentHealth(AgentHealthStatus.DOWN, Collections.emptyMap(), ex.getMessage(), Instant.now()));
                logger.error("Agent failed to start: {} - {}", agentName, ex.getMessage(), ex);
                Map<String, Object> details = new HashMap<>();
                details.put("event", "start_failed");
                details.put("error", ex.getMessage());
                AuditLogger.log("agent_start_failed", agentName, details);
            }
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping agent: {}", agentName);
            try {
                onStop();
            } catch (Exception ex) {
                logger.warn("Exception during agent stop: {} - {}", agentName, ex.getMessage(), ex);
            } finally {
                health.set(new AgentHealth(AgentHealthStatus.STOPPED));
                logger.info("Agent stopped: {}", agentName);
                Map<String, Object> details = new HashMap<>();
                details.put("event", "stop");
                AuditLogger.log("agent_stopped", agentName, details);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public AgentHealth getHealth() {
        return health.get();
    }

    // SmartLifecycle
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return 0;
    }

    // Hooks for subclasses
    protected abstract void onStart() throws Exception;
    protected abstract void onStop() throws Exception;
}
