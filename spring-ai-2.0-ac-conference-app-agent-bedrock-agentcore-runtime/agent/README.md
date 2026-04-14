# Spring AI 2.0 Conference Application Agent with Bedrock AgentCore Runtime

A Spring Boot application that implements an AI agent using Spring AI 2.0 framework, integrated with AWS Bedrock AgentCore Runtime and Model Context Protocol (MCP) for conference search and application management.

## Overview

This application provides an intelligent agent that can:
- Process natural language queries about conferences
- Interact with MCP servers for tool execution
- Support both short-term and long-term memory using AWS Bedrock AgentCore and Spring AI AgentCore
- Authenticate via AWS Cognito
- Execute tools synchronously or asynchronously
- Integrate with AWS Bedrock Converse API for LLM interactions

## Key Technologies

- **Spring Boot 4.0.5** - Application framework
- **Spring AI 2.0.0-M4** - AI integration framework
- **AWS Bedrock** - LLM provider (Amazon Nova, Claude Sonnet)
- **AWS Bedrock AgentCore** - Agent runtime and memory management
- **Spring AI AgentCore** - Bridges Spring AI and Bedrock AgentCore
- **Model Context Protocol (MCP)** - Tool integration protocol
- **AWS Cognito** - Authentication
- **Java 25** - Programming language

## Prerequisites

- Java 25 or higher
- Maven 3.6+
- AWS Account with access to:
  - AWS Bedrock
  - AWS Bedrock AgentCore
  - AWS Cognito
  - AWS STS
- Docker (optional, for containerized deployment)

## Configuration

Configure the application by editing `src/main/resources/application.properties`:

### Required AWS Configuration
```properties
aws.region=us-east-1
```

### Cognito Configuration
```properties
cognito.user.pool.name=UserPoolForAgentCoreMCP
cognito.user.pool.client.name=UserPoolClientWithUserAndPasswordForAgentCoreMCP
cognito.auth.token.resource.server.id=AgentCoreResourceServerId
```

### Bedrock Model Configuration
```properties
spring.ai.bedrock.converse.chat.options.model=amazon.nova-lite-v1:0
spring.ai.bedrock.converse.chat.options.max-tokens=100
spring.ai.bedrock.aws.timeout=10m
```

### AgentCore Gateway or Runtime
Choose one of the following:

**Option 1: AgentCore Gateway**
```properties
amazon.bedrock.agentcore.gateway.base.url=https://your-gateway-url.amazonaws.com
amazon.bedrock.agentcore.gateway.endpoint=/mcp
amazon.bedrock.agentcore.runtime.id=
```

**Option 2: AgentCore Runtime**
```properties
amazon.bedrock.agentcore.runtime.id=your-runtime-id
amazon.bedrock.agentcore.gateway.base.url=
```

### Memory Configuration
```properties
agentcore.memory.memory-id=long_term_memory_for_conference_application-K2Vg4aGfOo
agentcore.memory.total-events-limit=10
agentcore.memory.default-session=default-session-id-12345678
agentcore.memory.long-term.auto-discovery=true
```

## Building the Application

### Using Maven Wrapper (Recommended)

**Windows:**
```cmd
mvnw.cmd clean package
```

**Unix/Linux/macOS:**
```bash
./mvnw clean package
```

### Using System Maven
```bash
mvn clean package
```

The build produces a JAR file in the `target/` directory:
```
target/spring-ai-2.0-ac-conference-app-agent-bedrock-agentcore-runtime-0.0.1-SNAPSHOT.jar
```

## Running the Application

### Run Locally

**Windows:**
```cmd
mvnw.cmd spring-boot:run
```

**Unix/Linux/macOS:**
```bash
./mvnw spring-boot:run
```

Or run the JAR directly:
```bash
java -jar target/spring-ai-2.0-ac-conference-app-agent-bedrock-agentcore-runtime-0.0.1-SNAPSHOT.jar
```

```

The Dockerfile includes AWS OpenTelemetry instrumentation for observability.

## API Endpoints

The application exposes AgentCore invocation endpoints:

### Synchronous Invocation
- **Method:** POST
- **Endpoint:** Configured via AgentCore Runtime
- **Request Body:**
```json
{
  "prompt": "Your question about conferences"
}
```
- **Response:** String with agent's answer

### Asynchronous Invocation (Streaming)
- **Method:** POST
- **Endpoint:** Configured via AgentCore Runtime
- **Request Body:**
```json
{
  "prompt": "Your question about conferences"
}
```
- **Response:** Flux stream of agent's answer

## Features

### Memory Management
- **Short-term memory:** In-conversation context retention
- **Long-term memory:** Persistent memory across sessions via AgentCore

### Tool Integration
- Custom tools (e.g., DateTimeTools)
- MCP server tools (dynamically discovered)
- Synchronous and asynchronous tool execution

### Authentication
- OAuth 2.0 client credentials flow via AWS Cognito
- Automatic token retrieval and management

## Project Structure

```
agent/
├── src/main/java/dev/vkazulkin/
│   ├── SpringAIConferenceSearchAndApplicationOnAgentCoreRuntimeApplication.java
│   └── agent/
│       ├── controller/
│       │   ├── SpringAIAgentController.java
│       │   └── PromptRequest.java
│       ├── sdk/
│       │   └── InvokeRuntimeAgent.java
│       └── tools/
│           └── DateTimeTools.java
├── src/main/resources/
│   └── application.properties
├── Dockerfile
└── pom.xml
```

## Troubleshooting

### Common Issues

1. **Authentication Errors:** Verify Cognito user pool and client configuration
2. **MCP Connection Issues:** Check AgentCore Gateway/Runtime URL and network connectivity
3. **Model Access:** Ensure AWS credentials have Bedrock model access permissions
4. **Memory Errors:** Verify AgentCore memory ID exists and is accessible

### Logging

Enable debug logging by setting in `application.properties`:
```properties
logging.level.org.springframework.ai.mcp=DEBUG
logging.level.dev.vkazulkin=DEBUG
```

## License

See project license file for details.
