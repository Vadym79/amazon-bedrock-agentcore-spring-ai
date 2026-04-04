package dev.vkazulkin.agentcore.gateway;

import java.util.List;

import dev.vkazulkin.ConventionalDefaults;
import dev.vkazulkin.agentcore.runtime.AgentCoreRuntimeWithMCPStack;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.CustomJwtAuthorizer;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.Gateway;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.GatewayCredentialProvider;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.GatewayTarget;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.McpServerTargetConfiguration;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.OAuthConfiguration;
import software.constructs.Construct;

public class AgentCoreGatewayWithMCPTargetStack extends Stack {

    public AgentCoreGatewayWithMCPTargetStack(Construct scope, String appName,  StackProps stackProps) {
    	var id=ConventionalDefaults.stackName(appName, "gateway-with-mcp-server-target");
        super(scope, id, stackProps);
        System.out.println(" stack id "+id);
              
        var region=Stack.of(this).getRegion();
        var awsAccountId=(String)this.getNode().tryGetContext("awsAccountId");

        var gateway= Gateway.Builder.create(this, "Gateway-123")
           .gatewayName(appName.replace("_", "-")+ "-gateway")
           .authorizerConfiguration(CustomJwtAuthorizer.Builder
        		   .create().allowedClients(List.of(AgentCoreRuntimeWithMCPStack.userPoolClient.getUserPoolClientId()))
        		   .discoveryUrl(AgentCoreRuntimeWithMCPStack.COGNITO_DISCOVERY_URL).build())
            .role(AgentCoreRuntimeWithMCPStack.role)
           .description("AgenCore Runtime with MCP protocol for running conference search app").build();
          
        var endpoint="https://bedrock-agentcore."+region + 
        		".amazonaws.com/runtimes/" +
        		AgentCoreRuntimeWithMCPStack.runtime.getAgentRuntimeId()+
        		"/invocations?qualifier=DEFAULT&accountId="+Stack.of(this).getAccount();
        
       // currently no support for CreateOauth2CredentialProvider even in the 
       // CloudFormation, see the issue https://github.com/aws-cloudformation/cloudformation-coverage-roadmap/issues/2391
       var providerArn=ConventionalDefaults.replaceAWSAccountID((String)this.getNode().tryGetContext("agentcoreIdentityOutboundOAuthArn"),awsAccountId);
       var secretArn=ConventionalDefaults.replaceAWSAccountID((String)this.getNode().tryGetContext("secretArn"),awsAccountId);
                      
       var credentialProviderConfigs = List.of(GatewayCredentialProvider.fromOauthIdentityArn(OAuthConfiguration.builder()
        				                  .providerArn(providerArn)
        				                  .secretArn(secretArn)
        				                  .scopes(List.of())
        				                  .build()));
          		       
       GatewayTarget.Builder.create(this, "MCP-Target-123")
           .targetConfiguration(McpServerTargetConfiguration.create(endpoint))         
           .credentialProviderConfigurations(credentialProviderConfigs)
           .gatewayTargetName("mcp-target")
           .gateway(gateway)
           .build();
       
        //GatewayTarget.Builder.create(this, "MCP-Target-123")
       //.targetConfiguration((ApiGatewayTargetConfiguration.Builder.create()
    	//	   .restApi(null).build()));
          
       CfnOutput.Builder.create(this, "GatewayMCPURLOutput").value(gateway.getGatewayUrl()).build();       
    }
   
}