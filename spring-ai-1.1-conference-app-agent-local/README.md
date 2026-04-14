# Spring AI 1.1 Conference App Agent (Local)

A Spring Boot application that acts as an AI agent for conference search. It connects to an MCP (Model Context Protocol) server — either via **Amazon Bedrock AgentCore Gateway** or **AgentCore Runtime** — and uses **Amazon Bedrock** (Claude Sonnet or Amazon Nova) as the LLM backend via Spring AI.

The agent authenticates to the MCP server using OAuth2 client credentials obtained from **Amazon Cognito**, and exposes REST endpoints to answer conference-related questions synchronously or as a streaming response.

## Architecture

```
HTTP Client → Spring Boot Agent → Amazon Bedrock (Claude/Nova) via Spring AI
                                ↓
                         MCP Client (Streamable HTTP)
                                ↓
              AgentCore Gateway URL  OR  AgentCore Runtime endpoint
                         (authenticated via Cognito OAuth2)
```

Built-in tools available to the LLM:
- MCP tools from the remote MCP server (conference search, etc.)
- `Get_The_Current_Date` — local tool returning today's date

## Prerequisites

- Java 25+
- Maven 3.9+ (or use the included `mvnw` wrapper)
- AWS credentials configured (via environment variables, `~/.aws/credentials`, or IAM role)
- An Amazon Cognito User Pool named `UserPoolForAgentCoreMCP` with a client `UserPoolClientWithUserAndPasswordForAgentCoreMCP`
- Either an AgentCore Gateway URL or an AgentCore Runtime ID deployed in your AWS account

## Configuration

Edit `src/main/resources/application.properties`:

| Property | Description |
|---|---|
| `spring.ai.bedrock.aws.region` | AWS region for Bedrock |
| `spring.ai.bedrock.converse.chat.options.model` | Bedrock model ID |
| `cognito.user.pool.name` | Cognito User Pool name |
| `cognito.user.pool.client.name` | Cognito User Pool client name |
| `cognito.auth.token.resource.server.id` | Cognito resource server ID for OAuth2 scope |
| `amazon.bedrock.agentcore.gateway.url` | AgentCore Gateway MCP endpoint (leave empty if using Runtime) |
| `amazon.bedrock.agentcore.runtime.id` | AgentCore Runtime ID (takes precedence over Gateway URL if set) |
| `aws.region` | AWS region used for STS and AgentCore Runtime endpoint |

> At least one of `amazon.bedrock.agentcore.gateway.url` or `amazon.bedrock.agentcore.runtime.id` must be configured.

## Build

```bash
./mvnw clean package
```

On Windows:
```cmd
mvnw.cmd clean package
```

## Run

```bash
./mvnw spring-boot:run
```

Or run the packaged JAR:
```bash
java -jar target/spring-ai-1.1-conference-app-agent-local-0.0.1-SNAPSHOT.jar
```

The application starts on port `8080` by default.

## API Endpoints

### Health check
```
GET /ping
```
Returns `{"status": "healthy"}`.

### Streaming response (recommended)
```
GET /conference?prompt=<your question>
Content-Type: text/plain
```
Returns a streaming (`Flux<String>`) response from the AI agent.

### Synchronous response
```
GET /conference-sync?prompt=<your question>
Content-Type: text/plain
```
Returns the full agent response as a single string.

**Example:**
```bash
curl -H "Content-Type: text/plain" \
  "http://localhost:8080/conference?prompt=What+talks+are+scheduled+for+tomorrow?"
```
