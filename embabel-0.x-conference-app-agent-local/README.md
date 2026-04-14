# Embabel Conference Application Agent

A Spring Boot application that uses AI agents powered by AWS Bedrock and the Embabel Agent framework to automate conference talk management and applications. The application integrates with Model Context Protocol (MCP) servers to search for talks, create new talks, find conferences, and submit conference applications.

## Overview

This application provides two main AI-powered workflows:

1. **Search for Existing Talks and Apply to Conferences** - Searches for existing talks based on criteria and applies to relevant conferences
2. **Create New Talks and Apply to Conferences** - Creates new talks based on specifications and applies to relevant conferences

The agents use AWS Bedrock's Claude Sonnet and Amazon Nova models for natural language understanding and decision-making, communicating with backend services via MCP (Model Context Protocol).

## Architecture

### Key Components

- **Agents**
  - `SearchForTalksAndApplyForConferencesAgent` - Searches existing talks and applies to conferences
  - `CreateTalksAndApplyForConferencesAgent` - Creates new talks and applies to conferences
  - `AbstractConferenceAgent` - Base agent with common functionality

- **Controller**
  - `EmbabelAgentController` - REST endpoints for invoking agents

- **Services**
  - `McpToolService` - Manages MCP client connections and AWS Cognito authentication

- **Configuration**
  - `ConferenceConfig` - Conference-specific configuration
  - `DateTimeTools` - Date/time utility tools for agents

### Technology Stack

- **Framework**: Spring Boot 3.5.13
- **Java Version**: 25
- **AI Framework**: Embabel Agent 0.3.4
- **AI Models**: Spring AI 1.1.4 with AWS Bedrock
- **MCP Client**: Model Context Protocol for tool integration
- **Authentication**: AWS Cognito
- **Build Tool**: Maven

## Prerequisites

- Java 25 or higher
- Maven 3.6+
- AWS Account with:
  - AWS Bedrock access (Claude Sonnet 4.6 and Amazon Nova Pro models)
  - AWS Cognito User Pool configured
  - AWS Bedrock AgentCore Gateway or Runtime configured
- AWS credentials configured locally

## Configuration

Configure the following properties in `src/main/resources/application.properties`:

### AWS Bedrock Models
```properties
embabel.models.default-llm=us.anthropic.claude-sonnet-4-6
embabel.models.llms.best=us.anthropic.claude-sonnet-4-6
embabel.models.llms.balanced=us.amazon.nova-pro-v1:0
```

### AWS Configuration
```properties
spring.ai.bedrock.aws.region=us-east-1
aws.region=us-east-1
```

### Cognito Configuration
```properties
cognito.user.pool.name=<your-user-pool-name>
cognito.user.pool.client.name=<your-user-pool-client-name>
cognito.auth.token.resource.server.id=<your-resource-server-id>
```

### AgentCore Configuration
Choose one of the following:

**Option 1: AgentCore Gateway**
```properties
amazon.bedrock.agentcore.gateway.url=https://<your-gateway-url>.gateway.bedrock-agentcore.us-east-1.amazonaws.com/mcp
amazon.bedrock.agentcore.runtime.id=
```

**Option 2: AgentCore Runtime**
```properties
amazon.bedrock.agentcore.gateway.url=
amazon.bedrock.agentcore.runtime.id=<your-runtime-id>
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
- `embabel-0.x-conference-app-agent-local-0.0.1-SNAPSHOT.jar`

## Running the Application

### Using Maven

**Windows:**
```cmd
mvnw.cmd spring-boot:run
```

**Unix/Linux/macOS:**
```bash
./mvnw spring-boot:run
```

### Using JAR

```bash
java -jar target/embabel-0.x-conference-app-agent-local-0.0.1-SNAPSHOT.jar
```

The application starts on the default port (8080).

## API Endpoints

### 1. Apply to Conferences with Existing Talks

**Endpoint:** `GET /applyToConferencesWithExistingTalks`

**Parameters:**
- `prompt` (query parameter, text/plain) - Natural language request describing the talk search criteria and conference preferences

**Example:**
```bash
curl -X GET "http://localhost:8080/applyToConferencesWithExistingTalks?prompt=Find talks about Spring Boot and apply to Java conferences in 2024"
```

### 2. Apply to Conferences with New Talks

**Endpoint:** `GET /applyToConferencesWithNewTalks`

**Parameters:**
- `prompt` (query parameter, text/plain) - Natural language request describing the talk to create and conference preferences

**Example:**
```bash
curl -X GET "http://localhost:8080/applyToConferencesWithNewTalks?prompt=Create a talk about AWS Bedrock integration and apply to cloud conferences"
```

## How It Works

1. **User submits a natural language prompt** via REST API
2. **Agent Platform selects the appropriate agent** based on the request
3. **Agent extracts structured data** from the prompt using AWS Bedrock LLMs
4. **Agent invokes MCP tools** to search/create talks and find conferences
5. **Agent applies to conferences** with the identified talks
6. **Response is returned** with conference application details

## Logging

The application uses SLF4J with detailed logging for:
- Agent invocations and decisions
- MCP client communications
- AWS Cognito authentication
- LLM prompts and responses (when debug mode is enabled)

Log levels can be configured in `application.properties`:
```properties
logging.level.root=INFO
logging.level.org.springframework.ai.mcp=DEBUG
logging.level.io.modelcontextprotocol.client=DEBUG
```

## Development

### Project Structure
```
src/main/java/dev/vkazulkin/
├── embabel/
│   ├── agent/          # AI agent implementations
│   ├── config/         # Configuration classes
│   ├── controller/     # REST controllers
│   ├── domain/         # Domain models
│   ├── service/        # Business services
│   └── tool/           # Agent tools
└── EmbabelConferenceApplicationOnAgentCore.java  # Main application class
```

### Adding New Agents

1. Extend `AbstractConferenceAgent`
2. Annotate with `@Agent` and define name/description
3. Implement `@Action` methods for agent workflow
4. Register in Spring context

## Troubleshooting

### Common Issues

**Authentication Errors:**
- Verify AWS credentials are configured
- Check Cognito User Pool and Client settings
- Ensure Resource Server ID is correct

**MCP Connection Issues:**
- Verify AgentCore Gateway URL or Runtime ID
- Check network connectivity to AWS services
- Review MCP client debug logs

**Model Access Errors:**
- Ensure AWS Bedrock model access is enabled in your region
- Verify model IDs match available models

## License

This project is a demonstration application for AWS Bedrock AgentCore and Spring AI integration.

## Additional Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [AWS Bedrock Documentation](https://docs.aws.amazon.com/bedrock/)
- [Embabel Agent Framework](https://github.com/embabel/embabel-agent)
- [Model Context Protocol](https://modelcontextprotocol.io/)
