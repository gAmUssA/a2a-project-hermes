# Technical Guidelines for A2A Kafka Agent System Task Management

## Overview

This document provides technical instructions for working with the task list in `docs/tasks.md` for the A2A Kafka Agent System project. The task list is the primary tool for tracking development progress, ensuring quality standards, and maintaining project coordination.

## Task List Structure

### Task Numbering Convention
- **Phase.Section.Task**: `X.Y.Z` format (e.g., `1.1.1`, `2.3.4`)
- **Special Categories**: Use prefixes for cross-cutting tasks:
  - `DOC.X`: Documentation tasks
  - `QA.X`: Quality assurance tasks
  - `VAL.X`: Validation and acceptance tasks
  - `PREP.X`: Pre-production tasks
  - `PROD.X`: Production deployment tasks

### Task Status Management
- **Incomplete**: `- [ ]` (empty checkbox)
- **Complete**: `- [x]` (marked checkbox)
- **In Progress**: Add `(IN PROGRESS)` after task description
- **Blocked**: Add `(BLOCKED: reason)` after task description
- **Skipped**: Add `(SKIPPED: reason)` after task description

## Working with Tasks

### Before Starting a Task

1. **Consult Context7 MCP Server**: Always use the Context7 MCP server to get up-to-date documentation for any libraries, frameworks, or technologies you'll be working with
2. **Review Dependencies**: Check if prerequisite tasks are completed
3. **Understand Acceptance Criteria**: Reference the requirements document for detailed acceptance criteria
4. **Estimate Effort**: Consider complexity and dependencies
5. **Mark as In Progress**: Update task status to indicate active work

```markdown
- [ ] 1.1.1 Create Maven/Gradle multi-module project structure (IN PROGRESS)
```

### Task Completion Criteria

Each task must meet the following criteria before marking as complete:

#### Basic Requirements
- [ ] Code is readable and follows simple conventions
- [ ] Basic functionality works as expected
- [ ] Simple tests verify core functionality
- [ ] Basic documentation updated (README, comments)

### Task Completion Process

1. **Complete Implementation**: Finish all code changes
2. **Run Tests**: Execute full test suite locally
3. **Update Documentation**: Ensure all documentation is current
4. **Create Pull Request**: Submit changes for review
5. **Address Review Comments**: Make necessary adjustments
6. **Merge Changes**: Complete the integration process
7. **Mark Task Complete**: Update checkbox to `[x]`
8. **Update Dependencies**: Notify team of completed dependencies

```markdown
- [x] 1.1.1 Create Maven/Gradle multi-module project structure
```

## Phase Management

### Phase Prerequisites
Before starting a new phase, ensure:
- All critical tasks from previous phases are completed
- Infrastructure dependencies are in place
- Team has necessary skills and resources
- External dependencies (APIs, services) are available

### Phase Completion Criteria
A phase is considered complete when:
- All tasks within the phase are marked complete
- Phase-level integration testing is successful
- Documentation for the phase is complete and reviewed
- Stakeholder acceptance is obtained for phase deliverables


## Branch and Commit Strategy

### Branch Naming Convention
- **Feature branches**: `feature/task-X.Y.Z-description`
- **Bug fix branches**: `bugfix/task-X.Y.Z-description`
- **Documentation branches**: `docs/task-DOC.X-description`

### Commit Message Format
```
[Task X.Y.Z] Brief description of changes

Detailed description of what was implemented:
- Specific change 1
- Specific change 2
- Any important notes or considerations

Closes: #issue-number (if applicable)
```

### Example
```
[Task 1.1.1] Create Maven multi-module project structure

Implemented the basic project structure with the following modules:
- a2a-core: Core A2A protocol integration
- a2a-agents: Agent implementations
- a2a-orchestrator: Workflow orchestration
- a2a-monitoring: Monitoring and observability
- a2a-web: Web dashboard and APIs

Added parent POM with dependency management and common configuration.
```

## Testing Guidelines

### Unit Testing
- **Scope**: Individual classes and methods
- **Framework**: JUnit 5 for basic testing
- **Focus**: Test core functionality works as expected

### Integration Testing
- **Scope**: Basic component interactions and Kafka integration
- **Framework**: Spring Boot Test
- **Focus**: Verify end-to-end workflows function correctly

## Configuration Management

### Environment-Specific Configuration
- **Development**: `application-dev.yml`
- **Testing**: `application-test.yml`
- **Production**: `application-prod.yml`
- **Docker**: `application-docker.yml`

### Secret Management
- Use environment variables for sensitive data
- Never commit secrets to version control
- Document required environment variables
- Provide example configurations with placeholder values

### Configuration Validation
- Implement `@ConfigurationProperties` with validation
- Fail fast on startup with invalid configuration
- Provide clear error messages for configuration issues
- Document all configuration parameters with examples

## Monitoring and Observability

### Logging Standards
- **Framework**: SLF4J with Logback
- **Levels**: ERROR for failures, WARN for recoverable issues, INFO for important events, DEBUG for detailed tracing
- **Format**: Structured logging with correlation IDs
- **Sensitive Data**: Never log API keys, user data, or other sensitive information

### Metrics Collection
- **Framework**: Micrometer with appropriate registry
- **Custom Metrics**: Business-specific metrics (agent performance, workflow success rates)
- **Standard Metrics**: JVM, HTTP, database connection metrics
- **Dashboards**: Grafana dashboards for key metrics

### Distributed Tracing
- **Framework**: OpenTelemetry
- **Propagation**: Trace context through all service calls
- **Sampling**: Configurable sampling rates per environment
- **Correlation**: Link traces to logs and metrics

## Error Handling and Recovery

### Error Classification
- **Transient Errors**: Network timeouts, temporary service unavailability
- **Permanent Errors**: Invalid input, configuration errors, business rule violations
- **System Errors**: Out of memory, disk full, critical service failures

### Recovery Strategies
- **Retry Logic**: Exponential backoff for transient errors
- **Circuit Breakers**: Prevent cascade failures
- **Dead Letter Queues**: Handle permanently failed messages
- **Graceful Degradation**: Maintain core functionality during partial failures

## Security Guidelines

### Input Validation
- Validate all external inputs (API requests, Kafka messages)
- Sanitize data before processing or storage
- Use parameterized queries to prevent injection attacks
- Implement rate limiting for API endpoints

### Authentication and Authorization
- Secure API endpoints with appropriate authentication
- Implement role-based access control where needed
- Use HTTPS for all external communications
- Rotate API keys and certificates regularly

### Data Protection
- Encrypt sensitive data at rest and in transit
- Implement audit logging for sensitive operations
- Follow data retention policies
- Ensure compliance with relevant regulations

## Deployment Guidelines

### Basic Deployment Checklist
- [ ] Basic tests pass locally
- [ ] Documentation updated
- [ ] Docker Compose setup works

### Simple Deployment Process
1. **Local Testing**: Test functionality locally
2. **Docker Setup**: Verify Docker Compose works
3. **Basic Validation**: Check core features work

## Communication and Coordination

### Daily Standups
- Report completed tasks from previous day
- Identify current tasks and any blockers
- Coordinate dependencies with team members
- Escalate issues that need management attention

### Task Updates
- Update task status in real-time
- Comment on blocked tasks with specific reasons
- Notify team of completed dependencies
- Document any deviations from original plan

### Documentation Updates
- Keep task list synchronized with actual progress
- Update plan document when significant changes occur
- Maintain architecture decision records
- Update troubleshooting guides based on issues encountered

## Tools and Automation

### Recommended Tools
- **IDE**: IntelliJ IDEA or Eclipse with appropriate plugins
- **Build**: Maven or Gradle with wrapper scripts
- **Testing**: JUnit 5 for basic testing

## Troubleshooting Common Issues

### Build Issues
- **Problem**: Dependency conflicts
- **Solution**: Check dependency tree, exclude conflicting versions
- **Prevention**: Regular dependency updates, use BOM files

### Test Issues
- **Problem**: Flaky tests
- **Solution**: Identify timing issues, improve test isolation
- **Prevention**: Use proper test containers, avoid shared state

### Kafka Issues
- **Problem**: Consumer lag
- **Solution**: Check partition count, consumer configuration
- **Prevention**: Monitor consumer lag, implement proper error handling

### Performance Issues
- **Problem**: High latency
- **Solution**: Profile application, identify bottlenecks
- **Prevention**: Regular performance testing, monitoring

## Continuous Improvement

### Retrospectives
- Conduct regular retrospectives at phase completion
- Identify what worked well and areas for improvement
- Update guidelines based on lessons learned
- Share knowledge across team members

### Process Updates
- Review and update guidelines quarterly
- Incorporate feedback from team members
- Adapt to new tools and technologies
- Maintain alignment with industry best practices

---

## Quick Reference

### Task Status Symbols
- `[ ]` - Not started
- `[x]` - Complete
- `(IN PROGRESS)` - Currently being worked on
- `(BLOCKED: reason)` - Cannot proceed due to dependency
- `(SKIPPED: reason)` - Intentionally not implemented

### Key Commands
```bash
# Run all tests
./mvnw clean test

# Run integration tests
./mvnw clean verify -P integration-tests

# Build Docker image
docker build -t a2a-kafka-agent .

# Start development environment
docker-compose up -d kafka schema-registry
```

### Important Files
- `docs/requirements.md` - Detailed requirements and acceptance criteria
- `docs/plan.md` - Overall improvement plan and architecture
- `docs/tasks.md` - This task list
- `.junie/guidelines.md` - This guidelines document