package dev.vkazulkin.agent.sdk;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;
import software.amazon.awssdk.services.bedrockagentcore.model.InvokeAgentRuntimeRequest;
import software.amazon.awssdk.services.sts.StsClient;


public class InvokeRuntimeAgent {

	private static final String AGENT_RUNTIME_ARN="arn:aws:bedrock-agentcore:us-east-1:{AWS_ACCOUNT_ID}:runtime/spring_ai_ac_conference_application_runtime-a00QWV3i7t";
	
	public static void main(String[] args) throws Exception {

		//String payload =
			//"{\"prompt\":\"Please provide me with the list of conferences, including their IDs, with the Java topic happening in 2027, with the call for papers open today. Also, provide me with the list of my talks with this topic in the title. Finally, for each conference and talk retrieved, apply individually for the conference.\"}";

		String payload =
				"{\"prompt\":\"Please create a talk with some cool title (max 60 charachters long) and description (max 700 charachters long) about using Spring AI on Amazon Bedrock AgentCore service. Then provide me with the list of conferences including their ids with Java topic hapenning in 2027 with call for papers open today. Finally, for each conference, apply individually for the it with the talk just created.\"}";

		//String payload =
			//	"{\"prompt\":\"You recently applied for some conferences for me. Can you provide me with the details?.\"}";
				
		var httpClient=ApacheHttpClient.builder()
			    .connectionTimeout(Duration.ofMinutes(5))
			    .socketTimeout(Duration.ofMinutes(5))
			    .build();
					
		var bedrockAgentCoreClient = BedrockAgentCoreClient.builder()			
				.region(Region.US_EAST_1)
				.httpClient(httpClient)
				.build();

		var invokeAgentRuntimeRequest = InvokeAgentRuntimeRequest.builder()
				.agentRuntimeArn(replaceAWSAccountID(AGENT_RUNTIME_ARN))				 
				.qualifier("DEFAULT").contentType("application/json").payload(SdkBytes.fromUtf8String(payload)).build();
		try (var responseStream = bedrockAgentCoreClient
				.invokeAgentRuntime(invokeAgentRuntimeRequest)) {
			var text = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);

			System.out.println(text);
		}

	}

    private static String replaceAWSAccountID(String arn ) {
    	var replacedArn = arn.replace("{AWS_ACCOUNT_ID}", getAccountId());
    	System.out.println("replaced runtime arn "+replacedArn);
    	return replacedArn;
    }

	private static String getAccountId() {
		var stsClient = StsClient.builder().region(Region.US_EAST_1).build();
	    var awsAccountId= stsClient.getCallerIdentity().account();
	    System.out.println("AWS Account Id "+awsAccountId);
	    return awsAccountId;
	}
}
