# Conference Application Agent - AWS CDK Infrastructure

AWS CDK for Java infrastructure for Conference Search and Application Agent implemented with Spring AI 2.0 (with Spring AI Bedrock AgentCore) deployed on Amazon Bedrock AgentCore Runtime with MCP servers.

## Overview

This CDK project provisions the AWS infrastructure required to run the Conference Application Agent on Amazon Bedrock AgentCore Runtime. The agent acts as an MCP (Model Context Protocol) client to interact with tools for searching conferences and submitting applications.

## What This Project Does

This CDK application deploys three main infrastructure stacks:

1. **RuntimeWithMCPStack**: Provisions the AgentCore Runtime environment with MCP server integration
2. **ShortTermMemoryStack**: Sets up short-term memory storage for conversation context
3. **LongTermMemoryStack**: Configures long-term memory for persistent agent knowledge

## Prerequisites

- Java 25
- Maven 3.6+
- AWS Account with appropriate permissions
- AWS CLI configured with credentials
- Node.js and AWS CDK CLI installed (`npm install -g aws-cdk`)
- Docker image of the agent application pushed to ECR

## Configuration

Before deploying, configure the following in `cdk.json`:

```json
{
  "context": {
    "ecrImageURIForConferenceSearchAndApplicationAgent": "{AWS_ACCOUNT_ID}.dkr.ecr.us-east-1.amazonaws.com/spring-ai-2.0-ac-conference-search-and-application-agent-bedrock-agentcore-runtime:v204",
    "roleArnForTheAgentCoreRuntime": "arn:aws:iam::{AWS_ACCOUNT_ID}:role/spring-aiac-conference-search-application-agentcore-runtime-role"
  }
}
```

Replace `{AWS_ACCOUNT_ID}` with your actual AWS account ID.

## Build

Compile the CDK application:

```bash
mvn clean package
```

## Deploy

### Deploy All Stacks

Deploy all infrastructure stacks at once:

```bash
mvn clean package && cdk deploy -c awsAccountId={YOUR_AWS_ACCOUNT_ID} --all
```

Or use the convenience script:

```bash
./buildAndDeploy.sh
```

### Deploy Individual Stacks

Deploy specific stacks by name:

```bash
# Runtime with MCP Server
mvn clean package && cdk deploy spring-ai-ac-conference-application-agentcore-runtime-stack -c awsAccountId={YOUR_AWS_ACCOUNT_ID}

# Short-term Memory
mvn clean package && cdk deploy spring-ai-ac-conference-application-st-memory-stack -c awsAccountId={YOUR_AWS_ACCOUNT_ID}

# Long-term Memory
mvn clean package && cdk deploy spring-ai-ac-conference-application-lt-memory-stack -c awsAccountId={YOUR_AWS_ACCOUNT_ID}
```

## Available Stacks

- `spring-ai-ac-conference-application-agentcore-runtime-stack` - AgentCore Runtime with MCP integration
- `spring-ai-ac-conference-application-st-memory-stack` - Short-term memory infrastructure
- `spring-ai-ac-conference-application-lt-memory-stack` - Long-term memory infrastructure

## Destroy Infrastructure

Remove all deployed resources:

```bash
cdk destroy --all
```

Or use the convenience script:

```bash
./destroy.sh
```

## CDK Commands

- `cdk ls` - List all stacks in the app
- `cdk synth` - Synthesize CloudFormation templates
- `cdk diff` - Compare deployed stack with current state
- `cdk deploy` - Deploy stacks to AWS
- `cdk destroy` - Remove stacks from AWS

## Project Structure

```
.
├── src/main/java/dev/vkazulkin/
│   ├── CDKApp.java                          # Main CDK application entry point
│   ├── ConventionalDefaults.java            # Default configurations
│   └── agentcore/
│       ├── runtime/
│       │   └── RuntimeWithMCPStack.java     # AgentCore Runtime stack
│       └── memory/
│           ├── ShortTermMemoryStack.java    # Short-term memory stack
│           └── LongTermMemoryStack.java     # Long-term memory stack
├── cdk.json                                 # CDK configuration
├── pom.xml                                  # Maven dependencies
├── buildAndDeploy.sh                        # Deployment script
└── destroy.sh                               # Cleanup script
```

## Technology Stack

- **AWS CDK**: 2.246.0
- **Bedrock AgentCore Alpha**: 2.246.0-alpha.0
- **Java**: 25
- **Maven**: 3.x

## Region

Default deployment region: `us-east-1`

To change the region, modify the `stackProperties()` method in `CDKApp.java`.

## Troubleshooting

- Ensure AWS credentials are configured: `aws configure`
- Verify CDK is bootstrapped: `cdk bootstrap aws://{ACCOUNT_ID}/us-east-1`
- Check IAM permissions for CDK deployment
- Confirm ECR image exists and is accessible
- Review CloudFormation stack events in AWS Console for deployment errors
