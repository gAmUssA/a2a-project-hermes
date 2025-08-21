package com.a2a.kafka.agents.monitoring;

import com.a2a.kafka.agents.api.Agent;
import com.a2a.kafka.agents.core.AgentHealth;
import com.a2a.kafka.agents.core.AgentHealthStatus;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentsHealthIndicator implements HealthIndicator {

    private final List<Agent> agents;

    public AgentsHealthIndicator(List<Agent> agents) {
        this.agents = agents;
    }

    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        boolean anyDown = false;
        for (Agent agent : agents) {
            AgentHealth h = agent.getHealth();
            AgentHealthStatus status = h.getStatus();
            builder.withDetail(agent.getAgentName(), status.name());
            if (status == AgentHealthStatus.DOWN) {
                anyDown = true;
            }
        }
        if (anyDown) {
            return builder.down().build();
        }
        return builder.build();
    }
}
