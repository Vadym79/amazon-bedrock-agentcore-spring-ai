# IaC

AWS CDK infrastructure for Amazon Bedrock AgentCore projects with Spring AI 

## Configure

configure the properties in cdk.json which march our environment

## Deploy

```bash
# all stacks
mvn clean package && cdk deploy -c awsAccountId={YOUR_AWS_ACCOUINT_ID} --all

# available stacks names are: spring-ai-conference-search-agentcore-runtime-with-mcp-server-stack , spring-ai-conference-search-agentcore-gateway-with-mcp-server-target-stack

mvn clean package && cdk deploy {STACK_NAME} -c awsAccountId={YOUR_AWS_ACCOUINT_ID}
 
```


