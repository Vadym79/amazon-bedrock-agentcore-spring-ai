# IaC

AWS CDK for Java infrastructure for Conference Search And Application with Spring AI deployed on different Amazon Bedrock Agentcore MCP servers based on AgentCore Runtime and Agentcore Gateway 

## Configure

configure the properties in cdk.json which march our environment

## Deploy

```bash
# all stacks
mvn clean package && cdk deploy -c awsAccountId={YOUR_AWS_ACCOUINT_ID} --all

# available stacks names are: 
spring-ai-conference-search-agentcore-user-client-pool-stack
spring-ai-conference-search-agentcore-runtime-with-mcp-server-stack 
spring-ai-conference-search-agentcore-gateway-with-mcp-server-target-stack

mvn clean package && cdk deploy {STACK_NAME} -c awsAccountId={YOUR_AWS_ACCOUINT_ID} -c restApiId={YOUR_AMAZON_GATEWAY_REST_API_ID}
 
```


