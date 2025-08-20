# A2A Kafka Agent System - Configuration Guide

This document provides comprehensive information about all configuration parameters available in the A2A Kafka Agent System.

## Configuration Profiles

The system supports multiple configuration profiles for different environments:

- **dev** - Development environment (default)
- **test** - Testing environment
- **docker** - Docker containerized environment
- **prod** - Production environment

## Core Configuration Parameters

### A2A System Configuration

Configuration prefix: `a2a`

#### System Information
```yaml
a2a:
  system:
    name: "A2A Kafka Agent System"  # System name
    version: "1.0.0-SNAPSHOT"       # System version
```

**Environment Variables:**
- `A2A_SYSTEM_NAME` - Override system name
- `A2A_SYSTEM_VERSION` - Override system version

#### Topic Configuration
```yaml
a2a:
  topics:
    tasks: "a2a.tasks"       # Task processing topic
    replies: "a2a.replies"   # Reply messages topic
    events: "a2a.events"     # Event streaming topic
    registry: "a2a.registry" # Agent registry topic
```

**Environment Variables:**
- `A2A_TOPICS_TASKS` - Tasks topic name
- `A2A_TOPICS_REPLIES` - Replies topic name
- `A2A_TOPICS_EVENTS` - Events topic name
- `A2A_TOPICS_REGISTRY` - Registry topic name

#### Agent Configuration
```yaml
a2a:
  agents:
    translator:
      enabled: true           # Enable/disable translator agent
      max-concurrent: 10      # Max concurrent translations (1-100)
    summarizer:
      enabled: true           # Enable/disable summarizer agent
      max-concurrent: 10      # Max concurrent summarizations (1-100)
    llm:
      enabled: true           # Enable/disable general LLM agent
      max-concurrent: 10      # Max concurrent LLM requests (1-100)
```

**Environment Variables:**
- `A2A_TRANSLATOR_ENABLED` - Enable translator agent
- `A2A_TRANSLATOR_MAX_CONCURRENT` - Max concurrent translations
- `A2A_SUMMARIZER_ENABLED` - Enable summarizer agent
- `A2A_SUMMARIZER_MAX_CONCURRENT` - Max concurrent summarizations
- `A2A_LLM_ENABLED` - Enable LLM agent
- `A2A_LLM_MAX_CONCURRENT` - Max concurrent LLM requests

#### Orchestrator Configuration
```yaml
a2a:
  orchestrator:
    enabled: true                    # Enable/disable orchestrator
    max-chain-length: 10             # Max workflow chain length (1-50)
    timeout-seconds: 300             # Workflow timeout in seconds (10-3600)
    max-concurrent-workflows: 50     # Max concurrent workflows (1-1000)
```

**Environment Variables:**
- `A2A_ORCHESTRATOR_ENABLED` - Enable orchestrator
- `A2A_MAX_CHAIN_LENGTH` - Max workflow chain length
- `A2A_TIMEOUT_SECONDS` - Workflow timeout
- `A2A_MAX_CONCURRENT_WORKFLOWS` - Max concurrent workflows

### OpenAI Configuration

Configuration prefix: `openai`

```yaml
openai:
  api-key: "${OPENAI_API_KEY}"      # OpenAI API key (required)
  model: "gpt-3.5-turbo"            # OpenAI model name
  temperature: 0.7                  # Response randomness (0.0-2.0)
  max-tokens: 1000                  # Max response tokens (1-4096)
  timeout: 30s                      # Request timeout
  max-retries: 3                    # Max retry attempts (0-10)
```

**Environment Variables:**
- `OPENAI_API_KEY` - OpenAI API key (required)
- `OPENAI_MODEL` - OpenAI model name
- `OPENAI_TEMPERATURE` - Response temperature
- `OPENAI_MAX_TOKENS` - Max response tokens
- `OPENAI_TIMEOUT` - Request timeout
- `OPENAI_MAX_RETRIES` - Max retry attempts

### Kafka Configuration

Configuration prefix: `spring.kafka`

```yaml
spring:
  kafka:
    bootstrap-servers: "localhost:9092"  # Kafka broker addresses
    consumer:
      group-id: "a2a-agent-system"      # Consumer group ID
      auto-offset-reset: "earliest"      # Offset reset strategy
      max-poll-records: 500              # Max records per poll
    producer:
      acks: "all"                        # Acknowledgment mode
      retries: 10                        # Producer retries
      batch-size: 16384                  # Batch size in bytes
```

**Environment Variables:**
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka broker addresses
- `KAFKA_CONSUMER_GROUP_ID` - Consumer group ID
- `KAFKA_AUTO_OFFSET_RESET` - Offset reset strategy
- `KAFKA_MAX_POLL_RECORDS` - Max records per poll
- `KAFKA_PRODUCER_ACKS` - Producer acknowledgment mode
- `KAFKA_PRODUCER_RETRIES` - Producer retry count
- `KAFKA_BATCH_SIZE` - Producer batch size

### Spring Cloud Stream Configuration

Configuration prefix: `spring.cloud.stream`

```yaml
spring:
  cloud:
    stream:
      kafka:
        binder:
          brokers: "localhost:9092"      # Kafka brokers
          auto-create-topics: true       # Auto-create topics
      bindings:
        processAgentTasks-in-0:
          destination: "a2a.tasks"       # Input topic for tasks
          group: "a2a-agent-processors"  # Consumer group
        processAgentTasks-out-0:
          destination: "a2a.replies"     # Output topic for replies
```

### Server Configuration

```yaml
server:
  port: 8080                    # Server port
  servlet:
    context-path: "/api"        # Application context path
  tomcat:
    threads:
      max: 200                  # Max Tomcat threads
      min-spare: 10             # Min spare threads
```

**Environment Variables:**
- `SERVER_PORT` - Server port
- `SERVER_CONTEXT_PATH` - Application context path
- `SERVER_TOMCAT_MAX_THREADS` - Max Tomcat threads
- `SERVER_TOMCAT_MIN_SPARE_THREADS` - Min spare threads

### Management and Monitoring

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "health,info,metrics,prometheus"  # Exposed endpoints
  endpoint:
    health:
      show-details: "when-authorized"              # Health details visibility
  metrics:
    tags:
      application: "${spring.application.name}"   # Metric tags
      environment: "production"
```

**Environment Variables:**
- `MANAGEMENT_ENDPOINTS` - Exposed management endpoints
- `MANAGEMENT_HEALTH_SHOW_DETAILS` - Health details visibility
- `METRICS_PROMETHEUS_ENABLED` - Enable Prometheus metrics

### Logging Configuration

```yaml
logging:
  level:
    com.a2a.kafka: INFO          # A2A system log level
    org.springframework.kafka: WARN  # Kafka log level
    root: INFO                   # Root log level
  file:
    name: "/app/logs/a2a-kafka-agent.log"  # Log file path
    max-size: "100MB"            # Max log file size
    max-history: 30              # Max log file history
```

**Environment Variables:**
- `LOG_LEVEL_A2A` - A2A system log level
- `LOG_LEVEL_KAFKA` - Kafka log level
- `LOG_LEVEL_ROOT` - Root log level
- `LOG_FILE_PATH` - Log file path
- `LOG_FILE_MAX_SIZE` - Max log file size
- `LOG_FILE_MAX_HISTORY` - Max log file history

## Environment-Specific Examples

### Development Environment (.env.dev)
```bash
# OpenAI Configuration
OPENAI_API_KEY=your-dev-api-key
OPENAI_MODEL=gpt-3.5-turbo
OPENAI_TEMPERATURE=0.7

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# A2A Configuration
A2A_TRANSLATOR_ENABLED=true
A2A_SUMMARIZER_ENABLED=true
A2A_LLM_ENABLED=true

# Logging
LOG_LEVEL_A2A=DEBUG
LOG_LEVEL_KAFKA=DEBUG
```

### Docker Environment (.env.docker)
```bash
# OpenAI Configuration
OPENAI_API_KEY=your-docker-api-key
OPENAI_MODEL=gpt-3.5-turbo

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=kafka:29092
SCHEMA_REGISTRY_URL=http://schema-registry:8081

# A2A Configuration
A2A_ORCHESTRATOR_ENABLED=true
A2A_MAX_CHAIN_LENGTH=10

# Server Configuration
SERVER_PORT=8080
```

### Production Environment (.env.prod)
```bash
# OpenAI Configuration (required)
OPENAI_API_KEY=your-production-api-key
OPENAI_MODEL=gpt-4
OPENAI_TEMPERATURE=0.5
OPENAI_MAX_TOKENS=2000

# Kafka Configuration (required)
KAFKA_BOOTSTRAP_SERVERS=kafka-prod-1:9092,kafka-prod-2:9092,kafka-prod-3:9092
SCHEMA_REGISTRY_URL=https://schema-registry-prod:8081

# A2A Configuration
A2A_SYSTEM_NAME=A2A Kafka Agent System - Production
A2A_TRANSLATOR_MAX_CONCURRENT=20
A2A_SUMMARIZER_MAX_CONCURRENT=20
A2A_LLM_MAX_CONCURRENT=20
A2A_MAX_CONCURRENT_WORKFLOWS=100

# Server Configuration
SERVER_PORT=8080
SERVER_TOMCAT_MAX_THREADS=200

# Security Configuration
SECURITY_USER_NAME=admin
SECURITY_USER_PASSWORD=your-secure-password

# Logging Configuration
LOG_LEVEL_A2A=INFO
LOG_LEVEL_KAFKA=WARN
LOG_FILE_PATH=/app/logs/a2a-kafka-agent.log
```

## Configuration Validation

The system provides automatic validation for all configuration parameters:

### Validation Rules

1. **Required Fields**: API keys and essential configuration must be provided
2. **Range Validation**: Numeric values must be within specified ranges
3. **Format Validation**: String values must match expected formats
4. **Dependency Validation**: Related configurations must be consistent

### Validation Errors

When invalid configuration is detected, the application will:

1. **Fail Fast**: Stop startup immediately
2. **Clear Messages**: Provide descriptive error messages
3. **Suggest Fixes**: Include hints for correcting configuration

### Example Validation Error
```
***************************
APPLICATION FAILED TO START
***************************

Description:

Binding to target org.springframework.boot.context.properties.bind.BindException: 
Failed to bind properties under 'openai' to com.a2a.kafka.core.config.OpenAIProperties:

    Property: openai.temperature
    Value: 3.0
    Reason: Temperature must be between 0.0 and 2.0

Action:

Update your application's configuration. The following values are invalid:

    - openai.temperature: 3.0 (must be between 0.0 and 2.0)
```

## Configuration Best Practices

1. **Use Environment Variables**: Never hardcode sensitive values
2. **Profile-Specific Settings**: Use appropriate profiles for each environment
3. **Validate Early**: Test configuration in development
4. **Document Changes**: Update this guide when adding new parameters
5. **Secure Secrets**: Use proper secret management in production
6. **Monitor Configuration**: Track configuration changes and their impact

## Troubleshooting

### Common Configuration Issues

1. **Missing API Key**: Ensure `OPENAI_API_KEY` is set
2. **Kafka Connection**: Verify `KAFKA_BOOTSTRAP_SERVERS` is correct
3. **Port Conflicts**: Check `SERVER_PORT` is available
4. **Invalid Ranges**: Ensure numeric values are within valid ranges
5. **Profile Mismatch**: Verify correct profile is active

### Configuration Debugging

Enable debug logging for configuration:
```yaml
logging:
  level:
    org.springframework.boot.context.config: DEBUG
    org.springframework.boot.context.properties: DEBUG
```

This will show detailed information about configuration loading and binding.