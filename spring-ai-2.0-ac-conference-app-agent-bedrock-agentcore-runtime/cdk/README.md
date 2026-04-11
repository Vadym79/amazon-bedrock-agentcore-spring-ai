# IaC

AWS CDK for Java infrastructure for Conference Search And Application Agent implemented with Spring AI 2.0 (with Spring AI Bedrock Agentcore) deployed on Amazon Bedrock Agentcore MCP servers based on AgentCore Runtime
This agent acts as an MCP Client to talk to the tool to search for conferences and talk and apply to the conferences. Those tools are deploy on AgentCore Runrime and Gateway. 

## Configure

configure the properties in cdk.json which march our environment

## Deploy

```bash
# all stacks
mvn clean package && cdk deploy -c awsAccountId={YOUR_AWS_ACCOUINT_ID}  --all

# available stacks names are: 
spring-ai-conference-search-agentcore-runtime-with-mcp-server-stack 

mvn clean package && cdk deploy {STACK_NAME} -c awsAccountId={YOUR_AWS_ACCOUINT_ID}
 
```


