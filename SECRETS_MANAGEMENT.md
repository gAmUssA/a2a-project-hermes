# 🔐 Secrets Management Guide

## Overview
This document outlines best practices for managing secrets and API keys in the A2A project using Docker Compose and environment variables.

## ✅ Current Configuration

### Environment Variables Setup
- **`.env` file**: Contains all sensitive configuration
- **`.env.example`**: Template for required environment variables
- **`.gitignore`**: Ensures `.env` is never committed to version control

### Docker Compose Integration
```yaml
environment:
  - SPRING_AI_OPENAI_API_KEY=${OPENAI_API_KEY}
  - SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}
  - SPRING_KAFKA_BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS}
  - SPRING_CLOUD_SCHEMA_REGISTRY_CLIENT_ENDPOINT=${SCHEMA_REGISTRY_URL}
  - LOGGING_LEVEL_ROOT=${LOG_LEVEL}
```

## 🛡️ Security Best Practices

### 1. Environment File Management
- ✅ **DO**: Keep `.env` in `.gitignore`
- ✅ **DO**: Use `.env.example` as a template
- ✅ **DO**: Document all required variables
- ❌ **DON'T**: Commit actual API keys to version control
- ❌ **DON'T**: Share `.env` files via chat/email

### 2. API Key Rotation
- Regularly rotate API keys (monthly recommended)
- Update keys in `.env` file only
- Restart services after key rotation: `docker-compose restart`

### 3. Production Considerations
- Use Docker secrets or external secret management (AWS Secrets Manager, HashiCorp Vault)
- Implement least-privilege access principles
- Monitor API key usage and set up alerts

## 🔧 Usage Instructions

### Initial Setup
1. Copy the example file:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` with your actual values:
   ```bash
   # Edit the .env file with your API keys
   vim .env
   ```

3. Verify configuration:
   ```bash
   docker-compose config
   ```

### Adding New Secrets
1. Add to `.env`:
   ```
   NEW_API_KEY=your-secret-value
   ```

2. Add to `.env.example`:
   ```
   NEW_API_KEY=your-api-key-here
   ```

3. Update `docker-compose.yml`:
   ```yaml
   environment:
     - NEW_API_KEY=${NEW_API_KEY}
   ```

### Verification Commands
```bash
# Check if environment variables are loaded correctly
docker-compose config

# Verify specific service environment
docker-compose exec a2a-web env | grep OPENAI

# Test connectivity (if applicable)
docker-compose exec a2a-web curl -s http://localhost:8080/actuator/health
```

## 🚨 Troubleshooting

### Common Issues
1. **Variable not substituted**: Check `.env` file syntax (no spaces around `=`)
2. **Permission denied**: Ensure `.env` file is readable
3. **Service fails to start**: Verify all required variables are set

### Debug Commands
```bash
# Check environment variable loading
docker-compose config | grep -A 10 environment

# Inspect running container environment
docker inspect a2a-web-app | jq '.[0].Config.Env'
```

## 📋 Environment Variables Reference

### Required Variables
- `OPENAI_API_KEY`: OpenAI API key for AI services
- `SPRING_PROFILES_ACTIVE`: Spring Boot profile (default: docker)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka connection string
- `SCHEMA_REGISTRY_URL`: Schema Registry endpoint
- `LOG_LEVEL`: Application logging level

### Optional Variables
- `JWT_SECRET`: JWT signing secret (if authentication is implemented)
- `DB_PASSWORD`: Database password (if database is added)
- Additional API keys for other services

## 🔄 Maintenance

### Regular Tasks
- [ ] Review and rotate API keys monthly
- [ ] Update `.env.example` when adding new variables
- [ ] Audit environment variable usage
- [ ] Monitor for exposed secrets in logs

### Security Checklist
- [ ] `.env` is in `.gitignore`
- [ ] No hardcoded secrets in code
- [ ] API keys have appropriate permissions
- [ ] Monitoring is in place for key usage
