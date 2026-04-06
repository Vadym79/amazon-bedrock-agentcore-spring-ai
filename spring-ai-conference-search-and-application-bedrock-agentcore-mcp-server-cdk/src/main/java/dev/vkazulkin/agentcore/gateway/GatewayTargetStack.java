package dev.vkazulkin.agentcore.gateway;

import java.util.List;

import dev.vkazulkin.ConventionalDefaults;
import dev.vkazulkin.agentcore.runtime.RuntimeWithMCPStack;
import dev.vkazulkin.cognito.UserClientPoolStack;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.ApiGatewayHttpMethod;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.ApiGatewayTargetConfiguration;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.ApiGatewayToolConfiguration;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.ApiGatewayToolFilter;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.ApiGatewayToolOverride;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.ApiKeyAdditionalConfiguration;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.ApiKeyCredentialLocation;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.ApiKeyCredentialProviderProps;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.CustomJwtAuthorizer;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.Gateway;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.GatewayCredentialProvider;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.GatewayTarget;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.McpServerTargetConfiguration;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.OAuthConfiguration;
import software.constructs.Construct;

public class GatewayTargetStack extends Stack {

    public GatewayTargetStack(Construct scope, String appName,  StackProps stackProps) {
    	var id=ConventionalDefaults.stackName(appName, "gateway-with-mcp-server-target");
        super(scope, id, stackProps);
        System.out.println(" stack id "+id);
              
        var region=Stack.of(this).getRegion();
        var gateway= Gateway.Builder.create(this, "Gateway-123")
           .gatewayName(appName.replace("_", "-")+ "-gateway")
           .authorizerConfiguration(CustomJwtAuthorizer.Builder
        		   .create().allowedClients(List.of(UserClientPoolStack.userPoolClient.getUserPoolClientId()))
        		   .discoveryUrl(UserClientPoolStack.COGNITO_DISCOVERY_URL).build())
            .role(RuntimeWithMCPStack.role)
           .description("AgenCore Runtime with MCP protocol for running conference search app").build();
          
        var endpoint="https://bedrock-agentcore."+region + 
        		".amazonaws.com/runtimes/" +
        		RuntimeWithMCPStack.runtime.getAgentRuntimeId()+
        		"/invocations?qualifier=DEFAULT&accountId="+Stack.of(this).getAccount();
        
       // currently no support for CreateOauth2CredentialProvider even in the 
       // CloudFormation, see the issue https://github.com/aws-cloudformation/cloudformation-coverage-roadmap/issues/2391
       var oAuthProviderArn=ConventionalDefaults.getContextVariableValueWithReplacedAccountId(this, "agentcoreIdentityOutboundOAuthArn");
       var oAuthSecretArn=ConventionalDefaults.getContextVariableValueWithReplacedAccountId(this, "oAuthSecretArn");
    	              
       var oauthCredentialProviderConfigs = List.of(GatewayCredentialProvider
    		       .fromOauthIdentityArn(OAuthConfiguration.builder()
                  .providerArn(oAuthProviderArn)
                  .secretArn(oAuthSecretArn)
                  .scopes(List.of())
                  .build()));
      		       
       GatewayTarget.Builder.create(this, "MCP-Target-123")
           .targetConfiguration(McpServerTargetConfiguration.create(endpoint))         
           .credentialProviderConfigurations(oauthCredentialProviderConfigs)
           .gatewayTargetName("mcp-target")
           .description("AgentCore Runtime MCP Server Target ")
           .gateway(gateway)
           .build();
       
       
       var restApiId=(String)this.getNode().tryGetContext("restApiId");
       
       if(restApiId == null || restApiId.trim().isEmpty()) {
       	System.out.println("please provide your rest api id as as content to the call, for example: cdk deploy -c restApiId=ouklgti");
       }
       
       
       // currently no support for CreateAPIKeyCredentialProvider even in the 
       // CloudFormation, see the issue https://github.com/aws-cloudformation/cloudformation-coverage-roadmap/issues/2391
       var apiKeyProviderArn=ConventionalDefaults.getContextVariableValueWithReplacedAccountId(this, "agentcoreIdentityOutboundApiKeyArn");
       var apiKeySecretArn=ConventionalDefaults.getContextVariableValueWithReplacedAccountId(this, "apiKeySecretArn");
       var restApiStageName=ConventionalDefaults.getContextVariableValue(this, "restApiStageName");
         
       var apiKeyProviderConfigs = List.of(GatewayCredentialProvider
    		   .fromApiKeyIdentityArn(ApiKeyCredentialProviderProps.builder()
    		   .providerArn(apiKeyProviderArn)
    		   .secretArn(apiKeySecretArn)
    		   .credentialLocation(ApiKeyCredentialLocation
    				   .header(ApiKeyAdditionalConfiguration.builder()
    				   .credentialParameterName("x-api-key")
    				   .credentialPrefix(" ")
    				   .build()))
    		   .build()));
       
       GatewayTarget.Builder.create(this, "APIGATEWAY-Target-123")
           .targetConfiguration((ApiGatewayTargetConfiguration.Builder.create()
        		.apiGatewayToolConfiguration(ApiGatewayToolConfiguration.builder()
        			.toolFilters(List.of(
        					ApiGatewayToolFilter.builder()
        						.filterPath("/talks/{titleSubstring}")
        						.methods(List.of(ApiGatewayHttpMethod.GET))
        						.build(),
        					ApiGatewayToolFilter.builder()
        						.filterPath("/apply")
        						.methods(List.of(ApiGatewayHttpMethod.POST))
        						.build()))
                 	.toolOverrides(List.of(
                 			ApiGatewayToolOverride.builder()
        	                       .method(ApiGatewayHttpMethod.POST)
        	                       .name("apply-to-conferences-w-conference-id-talk-id")
        	                       .path("/apply")
        	                       .description("apply to the conference with conference Id and talk Id")
        	                       .build(), 
        	                 ApiGatewayToolOverride.builder()
        	                       .method(ApiGatewayHttpMethod.GET)
        	                       .name("get-talks-by-title-substring")
        	                       .path("/talks/{titleSubstring}")
        	                       .description("get application talk by its title substring.")
        	                       .build()))
        	                 .build())
           .restApi(RestApi.fromRestApiId(this, "APIGATEWAY-ID", restApiId)).stage(restApiStageName).build()))
           .credentialProviderConfigurations(apiKeyProviderConfigs)
           .gatewayTargetName("apigateway-target")
           .description("Amazon ApiGateway Target ")
           .gateway(gateway)
           .build();
          
       CfnOutput.Builder.create(this, "GatewayMCPURLOutput").value(gateway.getGatewayUrl()).build();       
    }
   
}