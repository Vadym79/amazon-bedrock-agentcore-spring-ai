package dev.vkazulkin.agent.sdk;

import java.nio.charset.StandardCharsets;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;
import software.amazon.awssdk.services.bedrockagentcore.model.InvokeAgentRuntimeRequest;


public class InvokeRuntimeAgent {

	private static final String AGENT_RUNTIME_ARN="arn:aws:bedrock-agentcore:us-east-1:265634257610:runtime/agentcore_runtime_spring_ai_demo-tD7f1W6RGi";
	
	public static void main(String[] args) throws Exception {

		//String payload = "{\"prompt\":\"Give me an overview of the order with the id equals 27\"}";
		String payload = "{\"prompt\":\"Give me an overview of the order with the id equals 550\"}";
		//String payload = "{\"prompt\":\"My name is Vadym. Today is a sunny weather\"}";
		//String payload = "{\"prompt\":\" Can you tell me my name, today's weather and summarize all orders you have information about \"}";
		//String payload = "{\"prompt\":\"I previously told you my name and today's weather. Can you please provide them to me\"}";
		//String payload = "{\"prompt\":\" Can you tell me my name and today's weather? \"}";
		//String payload = "{\"prompt\":\" I want to let you know that I love dogs and I have west highland white terrier dog at home \"}";
		//String payload = "{\"prompt\":\" Can you tell me what animals do I love? \"}";
		//String payload = "{\"prompt\":\" Can you tell me what dog breed do I have ? \"}";
		//String payload = "{\"prompt\":\" Please provide me with the summary of all orders you already have information about. Please don't stop at 2 orders \"}";
		//String payload = "{\"prompt\":\"I didn't ask you about the dog, but about an overview of the order with the id equals 23\"}";
		//String payload = "{\"prompt\":\"I asked you questions about several orders before. Please provide me with the summary of this information \"}";
		
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
