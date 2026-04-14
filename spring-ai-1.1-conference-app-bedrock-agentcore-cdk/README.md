# Conference Search App — AWS CDK Infrastructure

AWS CDK for Java (v2.246.0) that provisions the AWS infrastructure for the Conference Search and Application app, which runs as a Spring AI MCP server on Amazon Bedrock AgentCore.

## Architecture

Three CDK stacks are deployed in order:

1. **`spring-ai-conference-search-agentcore-user-client-pool-stack`**  
   Creates an Amazon Cognito User Pool and a machine-to-machine client (client credentials OAuth flow) used to authenticate calls to the AgentCore Runtime and Gateway.

2. **`spring-ai-conference-search-agentcore-runtime-with-mcp-server-stack`**  
   Creates an Amazon Bedrock AgentCore Runtime that runs the conference search Spring AI app as an MCP server from an ECR container image. JWT authorisation is wired to the Cognito User Pool created above.

3. **`spring-ai-conference-search-agentcore-gateway-with-mcp-server-target-stack`**  
   Creates an Amazon Bedrock AgentCore Gateway with two targets:
   - **MCP target** — forwards MCP protocol calls to the AgentCore Runtime endpoint, authenticated via an OAuth credential provider.
   - **API Gateway target** — exposes three REST operations (`GET /talks/{titleSubstring}`, `POST /talks`, `POST /apply`) from an existing Amazon API Gateway, authenticated via an API key credential provider.

## Prerequisites

- Java 25, Maven
- AWS CDK CLI (`npm install -g aws-cdk`)
- AWS CLI configured for `us-east-1`
- An ECR image of the conference search MCP server already pushed
- An IAM execution role for AgentCore Runtime already created
- An Amazon API Gateway REST API already deployed
- AgentCore Identity outbound OAuth and API key credential providers already created (see comments in `GatewayTargetStack.java` — CloudFormation does not yet support creating these automatically)

## Configure

Edit `cdk.json` and replace every `{AWS_ACCOUNT_ID}` placeholder with your 12-digit AWS account ID, then verify/update the remaining values:

| Key | Description |
|-----|-------------|
| `ecrImageURIForConferenceSearchAndApplicationAppAsMCPServer` | ECR image URI for the MCP server container |
| `roleArnForTheAgentCoreRuntime` | IAM role ARN used by the AgentCore Runtime |
| `cognitoDomainPrefix` | Cognito hosted-UI domain prefix (lowercase, hyphens only) |
| `agentcoreIdentityOutboundOAuthArn` | ARN of the AgentCore outbound OAuth credential provider |
| `oAuthSecretArn` | Secrets Manager ARN holding the OAuth client secret |
| `agentcoreIdentityOutboundApiKeyArn` | ARN of the AgentCore outbound API key credential provider |
| `apiKeySecretArn` | Secrets Manager ARN holding the API key secret |
| `restApiStageName` | API Gateway stage name (default: `prod`) |

## Build & Deploy

```bash
# Deploy all stacks
mvn clean package && cdk deploy \
  -c awsAccountId=<YOUR_AWS_ACCOUNT_ID> \
  -c restApiId=<YOUR_API_GATEWAY_REST_API_ID> \
  --all
```

Or use the helper script:

```bash
./buildAndDeploy.sh   # edit the placeholders in the script first
```

To deploy a single stack:

```bash
mvn clean package && cdk deploy <STACK_NAME> \
  -c awsAccountId=<YOUR_AWS_ACCOUNT_ID> \
  -c restApiId=<YOUR_API_GATEWAY_REST_API_ID>
```

Available stack names:
- `spring-ai-conference-search-agentcore-user-client-pool-stack`
- `spring-ai-conference-search-agentcore-runtime-with-mcp-server-stack`
- `spring-ai-conference-search-agentcore-gateway-with-mcp-server-target-stack`

## Stack Outputs

| Stack | Output | Description |
|-------|--------|-------------|
| user-client-pool | `CognitoUserPoolIdOutput` | Cognito User Pool ID |
| user-client-pool | `CognitoUserPoolClientIdOutput` | Cognito App Client ID |
| user-client-pool | `CognitoDiscoveryURLOutput` | OIDC discovery URL |
| runtime | `RuntimeIdOutput` | AgentCore Runtime ID |
| gateway | `GatewayMCPURLOutput` | AgentCore Gateway MCP endpoint URL |

## Destroy

```bash
./destroy.sh
# or
cdk destroy --all
```
