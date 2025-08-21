package com.a2a.kafka.agents.api;

import com.a2a.kafka.agents.core.AgentHealth;

/**
 * Base contract for all A2A agents.
 */
public interface Agent {
    /**
     * @return the logical name of the agent (used in logs and health)
     */
    String getAgentName();

    /**
     * Start the agent. Implementations should be idempotent.
     */
    void start();

    /**
     * Stop the agent. Implementations should be idempotent.
     */
    void stop();

    /**
     * @return true if the agent is currently running
     */
    boolean isRunning();

    /**
     * @return current health information for this agent
     */
    AgentHealth getHealth();
}
