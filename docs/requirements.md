# Requirements Document

## Introduction
This document defines the functional and non-functional requirements for a reference implementation that demonstrates Spring AI agents communicating using the A2A (Agent-to-Agent) protocol over Apache Kafka. The application leverages Spring AI 1.0.1 for LLM operations, the A2A Java SDK 0.2.5 for standardized agent communication, Spring Cloud Stream 2025.0.0 for reactive messaging, and Spring Boot 3.5.x (with virtual threads) as the runtime. It includes a lightweight monitoring dashboard delivered via Server-Sent Events (SSE) and optional MCP support for broader agent interoperability. The goal is to provide production-lean, reusable patterns (orchestrator-workers, routing, chaining) without reinventing capabilities already provided by the chosen frameworks and SDKs.

## Requirements

### Requirement 1: A2A Protocol Integration over Kafka
- User Story: As a user, I want agents to communicate using the A2A protocol over Kafka so that interactions are standardized, scalable, and interoperable across services.
- Acceptance Criteria:
  - WHEN an agent sends a command to another agent via the A2A SDK THEN the system SHALL publish an A2A-compliant envelope to the configured Kafka topic with a correlation taskId.
  - WHEN an agent processes an A2A command THEN the system SHALL return a reply message using the A2A SDK, preserving the original taskId and required headers.
  - WHEN the target agent is unavailable THEN the system SHALL retry according to configured backoff and SHALL surface a terminal error event after max retries.
  - WHEN message payloads are serialized THEN the system SHALL use the A2A SDK’s serialization to ensure schema compatibility.
  - WHEN the system receives a malformed A2A message THEN the system SHALL route it to a Dead Letter mechanism with diagnostic information.

### Requirement 2: Spring AI ChatClient Usage and Configuration
- User Story: As a user, I want agents to use Spring AI ChatClient with configurable models so that LLM capabilities are consistent, testable, and maintainable.
- Acceptance Criteria:
  - WHEN the application starts THEN the system SHALL load ChatClient configuration (model, temperature, retry) from application properties.
  - WHEN an agent invokes the ChatClient THEN the system SHALL apply default advisors (e.g., memory where configured) and prompt templates.
  - WHEN an OpenAI API key is not provided THEN the system SHALL fail fast with a clear configuration error.
  - WHEN a transient LLM error occurs THEN the system SHALL retry per configured attempts and backoff.
  - WHEN logging is enabled THEN the system SHALL avoid logging sensitive prompt or API key data.

### Requirement 3: Translator Agent
- User Story: As a user, I want a Translator Agent that translates input text into a requested language so that multilingual use cases are supported.
- Acceptance Criteria:
  - WHEN a translation task with fields {taskId, text, language} arrives on the input topic THEN the system SHALL invoke the Translator Agent and produce a translated text in a reply envelope preserving taskId.
  - WHEN the target language is not supported by the underlying model THEN the system SHALL return a structured error reply indicating unsupported language.
  - WHEN the input text is empty or only whitespace THEN the system SHALL return a validation error reply.
  - WHEN translation completes successfully THEN the system SHALL include metadata (sourceLang if detected, targetLang, model, timestamp) in the reply payload.
  - WHEN streaming/progress events are enabled THEN the system SHALL publish progress events to the events topic with taskId.

### Requirement 4: Summarizer Agent
- User Story: As a user, I want a Summarizer Agent that generates concise summaries so that long content can be quickly understood.
- Acceptance Criteria:
  - WHEN a summarization task with fields {taskId, text, lengthHint} arrives THEN the system SHALL produce a summary in a reply envelope preserving taskId.
  - WHEN lengthHint is provided (e.g., short, medium, long) THEN the system SHALL tailor the summary accordingly.
  - WHEN input text is too short to summarize usefully THEN the system SHALL return a graceful message indicating summarization may be unnecessary.
  - WHEN streaming is requested THEN the system SHALL emit interim summary chunks as events and a final reply upon completion.
  - WHEN the model fails mid-stream THEN the system SHALL emit an error event and a terminal error reply.

### Requirement 5: General-purpose LLM Agent
- User Story: As a user, I want a general-purpose LLM Agent to handle ad-hoc prompts so that the system can support flexible tasks beyond specialized agents.
- Acceptance Criteria:
  - WHEN an LLM task with prompt parameters is received THEN the system SHALL call ChatClient and return the generated content in a reply.
  - WHEN the prompt exceeds model token limits THEN the system SHALL handle gracefully by truncation or error, documenting the action in the reply.
  - WHEN safety filters are triggered THEN the system SHALL return a policy-compliant message and log a non-sensitive audit entry.

### Requirement 6: Orchestrator with Routing and Chaining
- User Story: As a user, I want an Orchestrator that determines an optimal agent chain so that multi-step workflows (e.g., translate then summarize) run automatically.
- Acceptance Criteria:
  - WHEN an orchestration request arrives THEN the system SHALL use a routing prompt/pattern to select an ordered agent chain based on the request.
  - WHEN the chain is determined THEN the system SHALL create and dispatch agent tasks in sequence or parallel as defined by the chain.
  - WHEN an upstream step fails THEN the system SHALL short-circuit and return an error with context to the caller.
  - WHEN a chain completes THEN the system SHALL aggregate intermediate results and return a final aggregated result.
  - WHEN observability is enabled THEN the system SHALL propagate trace/span context across all steps.

### Requirement 7: Spring Cloud Stream Functional Endpoints
- User Story: As a developer, I want functional bindings for processing, routing, and aggregating so that the messaging layer is reactive and maintainable.
- Acceptance Criteria:
  - WHEN the application starts THEN the system SHALL register functions (e.g., processAgentTasks, routeToAgents, aggregateResults) and bind them to Kafka topics per configuration.
  - WHEN messages are received on input bindings THEN the corresponding functions SHALL process and produce outputs on configured output bindings.
  - WHEN the binder cannot connect to Kafka THEN the system SHALL retry and expose health status as DOWN until recovered.
  - WHEN auto-create-topics is enabled THEN the system SHALL create required topics if missing.

### Requirement 8: Agent Registry Service (A2A)
- User Story: As a user, I want an Agent Registry so that capabilities and endpoints are discoverable.
- Acceptance Criteria:
  - WHEN the application starts THEN the system SHALL publish agent metadata (name, capabilities, version) to the registry topic using the A2A SDK.
  - WHEN an agent updates capabilities THEN the system SHALL publish an updated registry entry with the same identifier.
  - WHEN the registry topic is compacted THEN the system SHALL ensure the latest state is retained for each agent key.
  - WHEN querying registry data through internal APIs THEN the system SHALL return the last known capability set.

### Requirement 9: Kafka Topic Design and Configuration
- User Story: As an operator, I want topics to be well-defined so that throughput, retention, and ordering meet system needs.
- Acceptance Criteria:
  - WHEN producing to a2a.tasks, a2a.replies, a2a.events, and a2a.registry THEN the system SHALL use partitioning keyed by taskId (except registry which is compacted by key).
  - WHEN retention policies are applied THEN the system SHALL configure 7 days for tasks/replies, 1 day for events, and compacted for registry.
  - WHEN consumer lag increases THEN the system SHALL expose metrics to help diagnose throughput issues.

### Requirement 10: A2A Envelope Structure
- User Story: As a developer, I want a well-defined message envelope so that interoperability is guaranteed.
- Acceptance Criteria:
  - WHEN creating envelopes THEN the system SHALL include id, type (command|reply|event), from, to, taskId, method, payload, and timestamp as specified by the A2A SDK.
  - WHEN serializing envelopes THEN the system SHALL use SDK-provided serializers to ensure compatibility.
  - WHEN unknown fields are encountered THEN the system SHALL tolerate them without crashing and log at debug level.

### Requirement 11: Monitoring Dashboard via SSE
- User Story: As a user, I want a lightweight monitoring dashboard so that I can observe live agent events without complex infrastructure.
- Acceptance Criteria:
  - WHEN a client connects to /api/monitor/stream THEN the system SHALL initiate an SSE stream of AgentEvent objects mapped from Kafka event records.
  - WHEN no events are available THEN the system SHALL keep the SSE connection alive with heartbeat or idle behavior and not terminate prematurely.
  - WHEN an error occurs in the SSE pipeline THEN the system SHALL log the error and attempt up to 3 retries before closing the stream with an informative status.
  - WHEN the dashboard loads THEN the UI SHALL display event type, id, timestamp, and concise payload details with auto-scrolling and filtering controls.

### Requirement 12: Observability and Metrics
- User Story: As an SRE, I want tracing and metrics so that I can diagnose performance and reliability issues.
- Acceptance Criteria:
  - WHEN requests flow through agents THEN the system SHALL propagate OpenTelemetry context and emit spans for each processing step.
  - WHEN producing/consuming Kafka messages THEN the system SHALL emit Micrometer metrics (throughput, lag, errors) with application tags.
  - WHEN sampling is configured to 1.0 in non-prod THEN the system SHALL trace all requests; in prod, sampling SHALL be configurable.

### Requirement 13: Error Handling and Dead Letter Queue (DLQ)
- User Story: As an operator, I want robust error handling so that failures are contained and diagnosable.
- Acceptance Criteria:
  - WHEN message processing fails irrecoverably THEN the system SHALL route the message and error context to a DLQ or error topic.
  - WHEN validation errors occur (e.g., missing fields) THEN the system SHALL return structured error replies with codes and human-readable messages.
  - WHEN retries are exhausted THEN the system SHALL emit a terminal event and update monitoring accordingly.

### Requirement 14: MCP Client Support (Optional)
- User Story: As a user, I want optional MCP client support so that the system can interoperate with MCP-compatible agents/tools.
- Acceptance Criteria:
  - WHEN MCP is enabled via configuration THEN the system SHALL initialize MCP client components alongside A2A.
  - WHEN both A2A and MCP are enabled THEN the system SHALL keep protocols logically separated and avoid message format conflicts.
  - WHEN MCP is disabled THEN the system SHALL not load MCP beans and SHALL run without MCP dependencies at runtime.

### Requirement 15: Configuration and Secrets Management
- User Story: As an operator, I want configuration-driven behavior and secure secret handling so that deployments are safe and repeatable.
- Acceptance Criteria:
  - WHEN the application starts THEN the system SHALL read OpenAI API keys and related settings from environment variables or externalized configuration.
  - WHEN secrets are missing THEN the system SHALL fail fast with descriptive messages and SHALL not log secrets.
  - WHEN running under the docker profile THEN the system SHALL connect to the docker-compose Kafka and expose port 8080.

### Requirement 16: Deployment via Docker Compose (Dev)
- User Story: As a developer, I want to run the stack locally using Docker Compose so that I can test end-to-end quickly.
- Acceptance Criteria:
  - WHEN docker-compose up is executed for kafka and schema-registry THEN the system SHALL start those services and allow the app to connect.
  - WHEN the application container starts THEN it SHALL read OPENAI_API_KEY from the host environment and export the HTTP API on port 8080.
  - WHEN Kafka is not reachable from the container THEN health checks SHALL fail and logs SHALL indicate connection issues.

### Requirement 17: Security and Safety Considerations
- User Story: As a stakeholder, I want safety measures so that the system avoids leaking sensitive data and adheres to content policies.
- Acceptance Criteria:
  - WHEN prompts and responses are processed THEN the system SHALL avoid storing or logging sensitive content unless explicitly enabled for debugging.
  - WHEN policy violations are detected THEN the system SHALL return a compliant message and an audit-safe log entry.
  - WHEN external endpoints are called THEN the system SHALL use TLS where applicable and validate hosts per platform defaults.

### Requirement 18: Non-Functional Performance Targets
- User Story: As an SRE, I want performance targets so that capacity planning is informed.
- Acceptance Criteria:
  - WHEN the system processes messages under nominal load THEN it SHALL achieve >1000 msg/sec throughput measured by Micrometer.
  - WHEN agents respond under normal conditions THEN the 95th percentile response time SHALL be <2 seconds.
  - WHEN processing streams THEN consumer lag SHALL remain <500ms under steady state.
  - WHEN events are streamed to the dashboard THEN UI update latency SHALL be <1 second end-to-end.
  - WHEN errors occur THEN the overall error rate SHALL remain <1% during standard workloads.

### Requirement 19: Data Persistence and State
- User Story: As a developer, I want critical state to be durably recorded so that the system can resume and remain auditable.
- Acceptance Criteria:
  - WHEN agent capabilities are published THEN the registry topic SHALL be compacted to persist the latest state per agent key.
  - WHEN tasks and replies are produced THEN the system SHALL retain messages for 7 days to allow troubleshooting and replay in dev/test.
  - WHEN events are produced THEN the system SHALL retain them for 1 day to limit storage while supporting live dashboards.

### Requirement 20: Developer Experience and Quick Start
- User Story: As a developer, I want clear quick-start steps so that I can run and validate the demo easily.
- Acceptance Criteria:
  - WHEN the repository is cloned THEN the README or docs SHALL provide commands to run Kafka services and the application.
  - WHEN the application is running THEN a sample cURL to trigger orchestration SHALL be provided and SHALL return a plausible response structure.
  - WHEN the dashboard endpoint is opened THEN the user SHALL see live events without additional configuration.

