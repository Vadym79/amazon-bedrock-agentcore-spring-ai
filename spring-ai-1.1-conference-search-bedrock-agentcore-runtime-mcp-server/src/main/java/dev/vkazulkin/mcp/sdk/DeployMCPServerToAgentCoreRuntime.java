package dev.vkazulkin.mcp.sdk;

import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.bedrockagentcorecontrol.BedrockAgentCoreControlClient;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.AgentRuntimeArtifact;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.AuthorizerConfiguration;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.ContainerConfiguration;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.CreateAgentRuntimeRequest;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.CustomJWTAuthorizerConfiguration;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.UpdateAgentRuntimeRequest;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.NetworkConfiguration;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.NetworkMode;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.ProtocolConfiguration;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.ServerProtocol;

public class DeployMCPServerToAgentCoreRuntime {

	private static final String IAM_ROLE_ARN = "{IAM_ARN_ROLE}";
	private static final String CONTAINER_URI = "{AWS_ACCOUNT_ID}.dkr.ecr.{AWS_REGION}.amazonaws.com/{ECR_REPO}";

	private static final String CREATE_AGENT_RUNTIME_CONTAINER_URI = CONTAINER_URI + ":v1"; 
	private static final String UPDATE_AGENT_RUNTIME_CONTAINER_URI = CONTAINER_URI + ":v14";
	
	private static final String AGENT_RUNTIME_NAME = "{AGENT_RUNTIME_NAME}";
	private static final String AGENT_RUNTIME_ID = "{AGENT_RUNTIME_ID}";

	private static final String DISCOVERY_URL = "{DISCOVERY_URL}";
	private static final String[] ALLOWED_CLIENTS = new String[] { "ALLOWED_CLIENTS" };

	private static final BedrockAgentCoreControlClient bedrockAgentCoreControlClient = BedrockAgentCoreControlClient
			.builder().region(Region.US_EAST_1).build();

	private static void createAgentRuntime() {
		var request = CreateAgentRuntimeRequest.builder()
				.protocolConfiguration(getProtocolConfiguration())
				.authorizerConfiguration(getAuthorizerConfiguration())
				.agentRuntimeName(AGENT_RUNTIME_NAME)
				.roleArn(IAM_ROLE_ARN).networkConfiguration(getNetworkConfiguration())
				.agentRuntimeArtifact(getAgentRuntimeArtifact(CREATE_AGENT_RUNTIME_CONTAINER_URI)).build();
		var response = bedrockAgentCoreControlClient.createAgentRuntime(request);
		System.out.println("Create Agent Runtime response: " + response);
	}

	private static void updateAgentRuntime() {
		var request = UpdateAgentRuntimeRequest.builder()
				.protocolConfiguration(getProtocolConfiguration())
				.agentRuntimeId(AGENT_RUNTIME_ID)
				.authorizerConfiguration(getAuthorizerConfiguration())
				.roleArn(IAM_ROLE_ARN).networkConfiguration(getNetworkConfiguration())
				.agentRuntimeArtifact(getAgentRuntimeArtifact(UPDATE_AGENT_RUNTIME_CONTAINER_URI)).build();
		var response = bedrockAgentCoreControlClient.updateAgentRuntime(request);
		System.out.println("Update Agent Runtime response: " + response);
	}

	private static AuthorizerConfiguration getAuthorizerConfiguration() {
		var customJWTAuthorizerConfiguration = CustomJWTAuthorizerConfiguration.builder()
				.discoveryUrl(DISCOVERY_URL)
				.allowedClients(ALLOWED_CLIENTS).build();
		return AuthorizerConfiguration.builder().customJWTAuthorizer(customJWTAuthorizerConfiguration).build();
	}

	private static NetworkConfiguration getNetworkConfiguration() {
		return NetworkConfiguration.builder().networkMode(NetworkMode.PUBLIC).build();
	}

	private static ProtocolConfiguration getProtocolConfiguration() {
		return ProtocolConfiguration.builder().serverProtocol(ServerProtocol.MCP).build();
	}

	private static AgentRuntimeArtifact getAgentRuntimeArtifact(String containerURI) {
		return AgentRuntimeArtifact.fromContainerConfiguration(
				ContainerConfiguration.builder().containerUri(containerURI).build());
	}

	public static void main(String[] args) throws Exception {
		// createAgentRuntime();
		updateAgentRuntime();
	}

}