package com.a2a.kafka.core.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic configuration for A2A Agent System.
 * Defines all required topics with appropriate retention and partitioning strategies.
 */
@Configuration
public class KafkaTopicConfiguration {

    @Autowired
    private A2ASystemProperties a2aProperties;

    /**
     * A2A Tasks Topic - for task processing requests
     * Retention: 7 days
     * Partitions: 3 (for load distribution)
     * Replication: 1 (development), 3 (production)
     */
    @Bean
    public NewTopic a2aTasksTopic() {
        return TopicBuilder.name(a2aProperties.getTopics().getTasks())
                .partitions(3)
                .replicas(1)
                .config("retention.ms", String.valueOf(7 * 24 * 60 * 60 * 1000L)) // 7 days
                .config("cleanup.policy", "delete")
                .config("compression.type", "snappy")
                .config("max.message.bytes", "1048576") // 1MB
                .build();
    }

    /**
     * A2A Replies Topic - for task processing responses
     * Retention: 7 days
     * Partitions: 3 (for load distribution)
     * Replication: 1 (development), 3 (production)
     */
    @Bean
    public NewTopic a2aRepliesTopic() {
        return TopicBuilder.name(a2aProperties.getTopics().getReplies())
                .partitions(3)
                .replicas(1)
                .config("retention.ms", String.valueOf(7 * 24 * 60 * 60 * 1000L)) // 7 days
                .config("cleanup.policy", "delete")
                .config("compression.type", "snappy")
                .config("max.message.bytes", "1048576") // 1MB
                .build();
    }

    /**
     * A2A Events Topic - for real-time event streaming
     * Retention: 1 day (shorter for events)
     * Partitions: 6 (higher throughput for events)
     * Replication: 1 (development), 3 (production)
     */
    @Bean
    public NewTopic a2aEventsTopic() {
        return TopicBuilder.name(a2aProperties.getTopics().getEvents())
                .partitions(6)
                .replicas(1)
                .config("retention.ms", String.valueOf(24 * 60 * 60 * 1000L)) // 1 day
                .config("cleanup.policy", "delete")
                .config("compression.type", "snappy")
                .config("max.message.bytes", "524288") // 512KB (smaller for events)
                .build();
    }

    /**
     * A2A Registry Topic - for agent capability registration
     * Retention: Compacted (keep latest state)
     * Partitions: 1 (single partition for ordering)
     * Replication: 1 (development), 3 (production)
     */
    @Bean
    public NewTopic a2aRegistryTopic() {
        return TopicBuilder.name(a2aProperties.getTopics().getRegistry())
                .partitions(1)
                .replicas(1)
                .config("cleanup.policy", "compact")
                .config("compression.type", "snappy")
                .config("segment.ms", String.valueOf(24 * 60 * 60 * 1000L)) // 1 day segments
                .config("min.cleanable.dirty.ratio", "0.1") // Compact more frequently
                .config("delete.retention.ms", String.valueOf(24 * 60 * 60 * 1000L)) // 1 day tombstone retention
                .build();
    }

    /**
     * Dead Letter Queue Topic - for failed message processing
     * Retention: 30 days (longer for troubleshooting)
     * Partitions: 1 (single partition for DLQ)
     * Replication: 1 (development), 3 (production)
     */
    @Bean
    public NewTopic a2aDeadLetterTopic() {
        return TopicBuilder.name("a2a.dlq")
                .partitions(1)
                .replicas(1)
                .config("retention.ms", String.valueOf(30 * 24 * 60 * 60 * 1000L)) // 30 days
                .config("cleanup.policy", "delete")
                .config("compression.type", "snappy")
                .build();
    }
}