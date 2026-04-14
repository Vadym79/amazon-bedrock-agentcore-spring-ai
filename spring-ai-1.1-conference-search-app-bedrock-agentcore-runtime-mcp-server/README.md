# Conference Search MCP Server

A Spring AI 1.1 application that exposes conference search capabilities through Model Context Protocol (MCP) tools. This application enables natural language interactions with conference data via MCP clients like Amazon Q Developer or MCP Inspector.

## Overview

This application provides intelligent conference search functionality through MCP tools, allowing users to:
- Search conferences by topic
- Filter conferences by date ranges
- Find conferences with open Call for Papers (CFP)
- Query all available conferences

The application can run as:
- **Local MCP Server** - Connect with MCP Inspector or Amazon Q Developer
- **Amazon Bedrock AgentCore Runtime** - Deploy on AWS infrastructure

## Features

### MCP Tools

The application exposes four conference search tools:

1. **Conference_Search_Tool_By_Topic** - Search conferences by topic
2. **Conference_Search_Tool_By_Topic_And_Date** - Search by topic and date range
3. **Conference_Search_Tool_By_Topic_Date_CFP_Open** - Search by topic, dates, and open CFP status
4. **All_Conference_Search_Tool** - Retrieve all conferences

### Technology Stack

- **Spring Boot 4.0.5**
- **Spring AI 1.1.4** with MCP Server support
- **Java 25**
- **AWS SDK 2.42.22** (Bedrock AgentCore)
- **Maven** for build management

## Prerequisites

- Java 25 or higher
- Maven 3.6+ (or use included Maven wrapper)
- AWS credentials (for Bedrock AgentCore deployment)

## Building the Application

### Using Maven Wrapper (Recommended)

**Windows:**
```bash
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
spring-ai-1.1-conference-search-app-bedrock-agentcore-runtime-mcp-server-0.0.1-SNAPSHOT.jar
```

## Running the Application

### Local Execution

Run the application locally on port 8000:

```bash
java -jar target/spring-ai-1.1-conference-search-app-bedrock-agentcore-runtime-mcp-server-0.0.1-SNAPSHOT.jar
```

Or using Maven:

```bash
mvn spring-boot:run
```

The server will start at `http://localhost:8000`

## Configuration

The application is configured via `application.properties`:

```properties
# MCP Server Configuration
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.protocol=STATELESS

# Server Configuration
server.port=8000
server.address=0.0.0.0

# Logging
logging.level.root=INFO
logging.level.org.springframework.ai.mcp=DEBUG
```

### MCP Protocol Options

- **STATELESS** - For Amazon Bedrock AgentCore Runtime (default)
- **STREAMABLE** - For local MCP clients with streaming support

## Connecting MCP Clients

### Amazon Q Developer

Configure Amazon Q Developer to connect to the MCP server endpoint:
```
http://localhost:8000
```

### MCP Inspector

Use MCP Inspector to test and debug the MCP tools locally.

## Conference Data

Conference data is stored in `src/main/resources/conferences.json` and includes:
- Conference name and location
- Topics/tags
- Start and end dates
- Call for Papers dates
- Conference URLs

## AWS Deployment

The application includes AWS OpenTelemetry instrumentation for observability when deployed on Amazon Bedrock AgentCore Runtime:

- **Traces** - Exported to AWS X-Ray
- **Logs** - Exported to CloudWatch Logs
- **Metrics** - Custom metrics namespace

## Project Structure

```
src/main/java/dev/vkazulkin/
├── SpringMcpConferenceSearchServerApplication.java  # Main application
├── conference/
│   ├── Conference.java                              # Conference model
│   ├── Conferences.java                             # Conference collection
│   └── ConferenceSearchTools.java                   # MCP tool implementations
└── mcp/sdk/                                         # MCP SDK extensions
```

## Related Articles

For detailed explanations and architecture:

- [Spring AI with Amazon Bedrock - Part 1: Introduction and Sample Application](https://dev.to/aws-heroes/spring-ai-with-amazon-bedrock-part-1-introduction-and-the-sample-application-4hof)
- [Spring AI with Amazon Bedrock - Part 4: Exploring Model Context Protocol Streamable HTTP Transport](https://dev.to/aws-heroes/spring-ai-with-amazon-bedrock-part-4-exploring-model-context-protocol-streamable-http-transport-2o5h)

## License

See project license file for details.

## Author

Vadym Kazulkin
