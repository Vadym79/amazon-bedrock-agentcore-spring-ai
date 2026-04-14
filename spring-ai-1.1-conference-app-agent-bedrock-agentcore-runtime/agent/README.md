# Spring AI 1.1 Conference App Agent — Amazon Bedrock AgentCore Runtime

A Spring Boot AI agent that helps users search for tech conferences and manage talk submissions. It runs as an **Amazon Bedrock AgentCore Runtime** workload and connects to MCP (Model Context Protocol) tools via either an **AgentCore Gateway** or another **AgentCore Runtime** acting as an MCP server.

## What It Does

- Accepts natural-language prompts via a REST endpoint (`POST /invocations`)
- Uses **Amazon Nova Lite** (configurable) or **Claude Sonnet** as the LLM via Amazon Bedrock Converse API
- Connects to an MCP server to access conference search and talk application tools
- Authenticates to the MCP server using OAuth2 client credentials flow via **Amazon Cognito**
- Provides a built-in `Get_The_Current_Date` tool so the LLM can reason about open call-for-papers deadlines
- Maintains per-session chat memory for multi-turn conversations
- Exposes a `GET /ping` health endpoint required by AgentCore Runtime

## Architecture

```
Client
  │
  ▼
POST /invocations  (Spring Boot — this service)
  │
  ├─► Amazon Cognito  (obtain OAuth2 Bearer token)
  │
  ├─► MCP Server  (AgentCore Gateway  OR  AgentCore Runtime MCP endpoint)
  │     └─ conference search / talk application tools
  │
  └─► Amazon Bedrock Converse API  (Claude Sonnet / Nova Lite)
```

## Prerequisites

| Requirement | Version |
|---|---|
| Java | 25 |
| Maven | 3.9+ |
| Docker | (for container deployment) |
| AWS credentials | configured via environment / IAM role |

Required AWS resources (must exist before running):

- Amazon Cognito User Pool named `UserPoolForAgentCoreMCP` with a client named `UserPoolClientWithUserAndPasswordForAgentCoreMCP` and resource server ID `AgentCoreResourceServerId`
- Either an **AgentCore Gateway** URL or an **AgentCore Runtime** ID hosting the MCP conference-search server
- IAM permissions for Bedrock, Cognito, STS, and (if using Runtime endpoint) BedrockAgentCore

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# LLM model (set in SpringAIAgentController constructor)
# default: global.anthropic.claude-sonnet-4-6

# Choose ONE of the two MCP connection options:

# Option 1 — AgentCore Gateway
amazon.bedrock.agentcore.gateway.url=https://<your-gateway-url>/mcp

# Option 2 — AgentCore Runtime MCP Server (takes precedence if set)
amazon.bedrock.agentcore.runtime.id=<your-runtime-id>
aws.region=us-east-1

# Cognito settings (must match your AWS resources)
cognito.user.pool.name=UserPoolForAgentCoreMCP
cognito.user.pool.client.name=UserPoolClientWithUserAndPasswordForAgentCoreMCP
cognito.auth.token.resource.server.id=AgentCoreResourceServerId
```

## Build

```bash
./mvnw clean package
```

The fat JAR is produced at:
```
target/spring-ai-1.1-conference-app-agent-bedrock-agentcore-runtime-0.0.1-SNAPSHOT.jar
```

## Run Locally

```bash
./mvnw spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/spring-ai-1.1-conference-app-agent-bedrock-agentcore-runtime-0.0.1-SNAPSHOT.jar
```

The service starts on port **8080** by default.

### Test the endpoints

Health check:
```bash
curl http://localhost:8080/ping
```

Send a prompt (synchronous):
```bash
curl -X POST http://localhost:8080/invocations \
  -H "Content-Type: text/plain" \
  -d "List Java conferences in 2026 with call for papers open today"
```

```

Run the container (pass AWS credentials via environment variables or mount `~/.aws`):
```bash
docker run -p 8080:8080 \
  -e AWS_ACCESS_KEY_ID=<key> \
  -e AWS_SECRET_ACCESS_KEY=<secret> \
  -e AWS_SESSION_TOKEN=<token> \
  conference-agent
```

The Dockerfile includes the **AWS OpenTelemetry Java agent** for automatic tracing to AWS X-Ray and log forwarding to CloudWatch Logs.

## Deploy to Amazon Bedrock AgentCore Runtime

1. Push the Docker image to Amazon ECR.
2. Create (or update) an AgentCore Runtime pointing to the ECR image.
3. Set the runtime ID in `application.properties` (or leave it empty and use the Gateway URL instead).
4. AgentCore Runtime will call `GET /ping` for health checks and `POST /invocations` for inference requests.

## SDK Utility — InvokeRuntimeAgent

`InvokeRuntimeAgent.java` is a standalone main-class utility for invoking the deployed AgentCore Runtime directly via the AWS SDK. Before running it, replace `{AWS_ACCOUNT_ID}` in the `AGENT_RUNTIME_ARN` constant or let the code resolve it automatically via STS.

```bash
# Compile and run from your IDE or:
mvn exec:java -Dexec.mainClass="dev.vkazulkin.agent.sdk.InvokeRuntimeAgent"
```

## Key Dependencies

| Library | Purpose |
|---|---|
| `spring-ai-starter-model-bedrock-converse` | Amazon Bedrock LLM integration |
| `spring-ai-starter-mcp-client-webflux` | Async MCP client over HTTP/SSE |
| `software.amazon.awssdk:bedrockagentcore` | AgentCore Runtime invocation SDK |
| `software.amazon.awssdk:cognitoidentityprovider` | OAuth2 token retrieval |
| AWS OpenTelemetry Java Agent | Distributed tracing & log forwarding |
