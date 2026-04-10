package dev.vkazulkin.agent.sdk;

import java.nio.charset.StandardCharsets;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;
import software.amazon.awssdk.services.bedrockagentcore.model.InvokeAgentRuntimeRequest;


public class InvokeRuntimeAgent {

	private static final String AGENT_RUNTIME_ARN="arn:aws:bedrock-agentcore:us-east-1:265634257610:runtime/agentcore_runtime_spring_ai_demo-tD7f1W6RGi";
	
	//private static final String AGENT_RUNTIME_ARN="arn:aws:bedrock-agentcore:us-east-1:265634257610:runtime/spring_mcp_conference_search_mcp_server_iam_auth-d9nSELAw29";
	
	public static void main(String[] args) throws Exception {

		String payload = "{\"prompt\":\"Give me an overview of the order with the id equals 210\"}";
		
		//String payload = "{please provide me with the list of conferences with Java topic hapenning in 2026}";
		BedrockAgentCoreClient bedrockAgentCoreClient = BedrockAgentCoreClient.builder().region(Region.US_EAST_1)
				.build();

		var invokeAgentRuntimeRequest = InvokeAgentRuntimeRequest.builder()
				.agentRuntimeArn(AGENT_RUNTIME_ARN)
				.qualifier("DEFAULT").contentType("application/json").payload(SdkBytes.fromUtf8String(payload)).build();
		try (var responseStream = bedrockAgentCoreClient
				.invokeAgentRuntime(invokeAgentRuntimeRequest)) {
			var text = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);

			System.out.println(text);
		}

	}

}
