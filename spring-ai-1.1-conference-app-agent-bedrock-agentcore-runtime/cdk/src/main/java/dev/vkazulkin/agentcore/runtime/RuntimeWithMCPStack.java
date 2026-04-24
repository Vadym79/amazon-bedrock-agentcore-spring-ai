package dev.vkazulkin.agentcore.runtime;

import dev.vkazulkin.ConventionalDefaults;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.AgentRuntimeArtifact;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.ProtocolType;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.Runtime;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.RuntimeAuthorizerConfiguration;
import software.amazon.awscdk.services.iam.Role;
import software.constructs.Construct;

public class RuntimeWithMCPStack extends Stack {
	
    public RuntimeWithMCPStack(Construct scope, String appName,  StackProps stackProps) {
    	var id=ConventionalDefaults.stackName(appName, "agentcore-runtime");
        super(scope, id, stackProps);  
        System.out.println(" stack id "+id);
        
        var ecrImageURI=ConventionalDefaults.getContextVariableValueWithReplacedAccountId(this, "ecrImageURIForConferenceSearchAndApplicationAgent");     		
        var roleArnForTheAgentCoreRuntime=ConventionalDefaults.getContextVariableValueWithReplacedAccountId(this, "roleArnForTheAgentCoreRuntime");
       
        // The runtime, by default, creates ECR permissions only for the repository available in the account where the stack is being deployed
        var agentRuntimeArtifact = AgentRuntimeArtifact.fromImageUri(ecrImageURI);
        var role= Role.fromRoleArn(this,"roleArnForTheAgentCoreRuntimeRole", roleArnForTheAgentCoreRuntime);
     
        // Create runtime using the built image
        var runtime = Runtime.Builder.create(this, "MCPRuntime-125")
                .runtimeName(appName.replace("-", "_")+ "_runtime")
                .authorizerConfiguration(RuntimeAuthorizerConfiguration.usingIAM())
                .description("AgenCore Runtime with MCP protocol for running conference app")
                .protocolConfiguration(ProtocolType.HTTP)
                .agentRuntimeArtifact(agentRuntimeArtifact)
                .executionRole(role)
                .build();
        
        CfnOutput.Builder.create(this, "RuntimeIdOutput").value(runtime.getAgentRuntimeId()).build();
        CfnOutput.Builder.create(this, "RuntimeARNOutput").value(runtime.getAgentRuntimeArn()).build();
     }  
}