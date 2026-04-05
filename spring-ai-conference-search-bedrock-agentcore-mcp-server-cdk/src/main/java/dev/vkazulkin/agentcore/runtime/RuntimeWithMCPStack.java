package dev.vkazulkin.agentcore.runtime;


import java.util.List;

import dev.vkazulkin.ConventionalDefaults;
import dev.vkazulkin.cognito.UserClientPoolStack;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.AgentRuntimeArtifact;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.ProtocolType;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.Runtime;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.RuntimeAuthorizerConfiguration;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.Role;
import software.constructs.Construct;

public class RuntimeWithMCPStack extends Stack {
	
	public static IRole role;
	
	public static Runtime runtime;

    public RuntimeWithMCPStack(Construct scope, String appName,  StackProps stackProps) {
    	var id=ConventionalDefaults.stackName(appName, "runtime-with-mcp-server");
        super(scope, id, stackProps);   
        System.out.println(" stack id "+id);
        var awsAccountId=(String)this.getNode().tryGetContext("awsAccountId");
      
        if(awsAccountId == null || awsAccountId.trim().isEmpty()) {
        	System.out.println("please provide your aws account id as as content to the call, for example: cdk deploy -c awsAccountId=1234567890101");
        }
        
          var ecrImageURI=ConventionalDefaults.replaceAWSAccountID((String)this.getNode().tryGetContext("ecrImageURIForConferenceSearchAppAsMCPServer"), awsAccountId);
        var roleArnForTheAgentCoreRuntime=ConventionalDefaults.replaceAWSAccountID((String)this.getNode().tryGetContext("roleArnForTheAgentCoreRuntime"), awsAccountId);
       
        // The runtime by default create ECR permission only for the repository available in the account the stack is being deployed
        var agentRuntimeArtifact = AgentRuntimeArtifact.fromImageUri(ecrImageURI);
        role= Role.fromRoleArn(this,"roleArnForTheAgentCoreRuntimeRole", roleArnForTheAgentCoreRuntime);
     
        // Create runtime using the built image
        runtime = Runtime.Builder.create(this, "MCPRuntime-123")
                .runtimeName(appName.replace("-", "_")+ "_runtime")
                .authorizerConfiguration(RuntimeAuthorizerConfiguration.usingJWT(UserClientPoolStack.COGNITO_DISCOVERY_URL, List.of(UserClientPoolStack.userPoolClient.getUserPoolClientId()), null))
                .description("AgenCore Runtime with MCP protocol for running conference search app")
                .protocolConfiguration(ProtocolType.MCP)
                .agentRuntimeArtifact(agentRuntimeArtifact)
                .executionRole(role)
                .build();
        
        CfnOutput.Builder.create(this, "RuntimeIdOutput").value(runtime.getAgentRuntimeId()).build();           
     }  
}