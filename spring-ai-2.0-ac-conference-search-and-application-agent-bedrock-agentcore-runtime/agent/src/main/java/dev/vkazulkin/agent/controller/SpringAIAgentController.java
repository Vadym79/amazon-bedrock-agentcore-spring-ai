package dev.vkazulkin.agent.controller;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agentcore.annotation.AgentCoreInvocation;
import org.springaicommunity.agentcore.context.AgentCoreContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.vkazulkin.agent.tools.DateTimeTools;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolClientRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolClientsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolClientDescription;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolClientType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolDescriptionType;
import software.amazon.awssdk.services.sts.StsClient;


@RestController
public class SpringAIAgentController {

	private static final Logger logger = LoggerFactory.getLogger(SpringAIAgentController.class);

	private final ChatClient chatClient;

	@Value("${cognito.user.pool.name}")
	private String USER_POOL_NAME;
	
	@Value("${cognito.user.pool.client.name}")
	private String USER_POOL_CLIENT_NAME;
	
	@Value("${cognito.auth.token.resource.server.id}")
	private String RESOURCE_SERVER_ID;

	@Value("${amazon.bedrock.agentcore.gateway.base.url}")
	private String AGENTCORE_GATEWAY_BASE_URL;
	
	@Value("${amazon.bedrock.agentcore.gateway.endpoint}")
	private String AGENTCORE_GATEWAY_ENDPOINT;
	
	@Value("${amazon.bedrock.agentcore.runtime.id}")
	private String AGENTCORE_RUNTIME_ID;

	@Value("${aws.region}")
	private String AWS_REGION;
	
	private static final CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
			.region(Region.US_EAST_1).build();
	
	private static final StsClient stsClient = StsClient.builder().region(Region.US_EAST_1).build();
	  
	private static final ObjectMapper objectMapper = new ObjectMapper();

	 // to include custom session id into the conversation. 'actorId' or 'actorId:sessionId'
	 private final String CONVERSATION_ID="default-actor-id-12345678:default-session-id-12345678";
	
	public SpringAIAgentController(ChatClient.Builder builder, ChatMemory chatMemory) {
		var options = ToolCallingChatOptions.builder()
				 //.model("amazon.nova-pro-v1:0")
				.model("global.anthropic.claude-sonnet-4-6")
				.maxTokens(2000).build();

		this.chatClient = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
				.defaultOptions(options)
				// .defaultSystem(SYSTEM_PROMPT)
				//short term memory
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())	
				.build();

	}


	/**
	 * POST method which has a prompt as an input parameter and outputs the agent response synchronously
	 * 
	 * @param prompt - prompt
	 * @return agent answer
	 */
	@AgentCoreInvocation
	public String invokeSync(PromptRequest promptRequest, AgentCoreContext agentCoreContext) {
		logger.info("invocations endpoint with prompt: " + promptRequest.prompt());
		String token = getAuthTokenViaHttpClient();
		try (var client = McpClient.sync(getMcpClientTransport(token)).build()) {
			client.initialize();
			var toolsResult = client.listTools();
			for (var tool : toolsResult.tools()) {
				logger.info("tool found " + tool);
			}       
			var syncMcpToolCallbackProvider = SyncMcpToolCallbackProvider.builder().mcpClients(client).build();
			
			var toolCallbacks = concatWithStream(syncMcpToolCallbackProvider.getToolCallbacks(), ToolCallbacks.from(new DateTimeTools()));

			return this.chatClient.prompt().user(promptRequest.prompt())
					.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, CONVERSATION_ID))
					.toolCallbacks(toolCallbacks)
					.call().content();
		}
	}

	/**
	 *  POST method which has a prompt as an input parameter and outputs the agent response asynchronously
	 * 
	 * @param prompt - prompt
	 * @return asynchronous agent answer
	 */
	//@AgentCoreInvocation
	public Flux<String> invoceAsync(PromptRequest promptRequest, AgentCoreContext agentCoreContext) {
		logger.info("invocations endpoint with prompt: " + promptRequest.prompt());
		String token = getAuthTokenViaHttpClient();
		if (token == null) {
			throw new RuntimeException("can't obtain authorization token");
		}
		var client = McpClient.async(getMcpClientTransport(token)).build();
		client.initialize();
		var toolsResult = client.listTools(); 
		for (var tool : toolsResult.block().tools()) { 
			logger.info("tool found " + tool); 
		}
		 
		var asyncMcpToolCallbackProvider = AsyncMcpToolCallbackProvider.builder().mcpClients(client)
				/*
				 * .toolFilter(new McpToolFilter() {
				 * 
				 * @Override public boolean test(McpConnectionInfo info, Tool tool) { return
				 * tool.name().toLowerCase().contains("order"); } })
				 */
				.build();

		var toolCallbacks = concatWithStream(asyncMcpToolCallbackProvider.getToolCallbacks(), ToolCallbacks.from(new DateTimeTools()));
		var content = this.chatClient.prompt()
				 .user(promptRequest.prompt())
				 .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, CONVERSATION_ID))
				 .toolCallbacks(toolCallbacks).stream().content();

		// client.close();
		return content;
	}
	
	
	/**
	 * concatenate 2 arrays of the same type T
	 * 
	 * @param <T>
	 * @param array1
	 * @param array2
	 * @return array containing the elements of the both arrays
	 */
	private static <T> T[] concatWithStream(T[] array1, T[] array2) {
	    return Stream.concat(Arrays.stream(array1), Arrays.stream(array2))
	      .toArray(size -> (T[]) Array.newInstance(array1.getClass().getComponentType(), size));
	}

	/**
	 * returns streamable http mcp client transport
	 * 
	 * @param token -bearer authorization token
	 * @return streamable http mcp client transport
	 */
	private McpClientTransport getMcpClientTransport(String token) {
		
		var mcpServerConfig= this.getMCPServerConfig();
		logger.info("MCP Server config: " + mcpServerConfig);
		String headerValue = "Bearer " + token;
		
	    var httpRequestBuilder = HttpRequest.newBuilder().header("Authorization", headerValue);
		
		return HttpClientStreamableHttpTransport.builder(mcpServerConfig.baseUrl)
				.connectTimeout(Duration.ofMinutes(3))
				.endpoint(mcpServerConfig.endpoint)		
				.requestBuilder(httpRequestBuilder)
				.build();
	}

	
	private MCPServerConfig getMCPServerConfig() {
		if(AGENTCORE_RUNTIME_ID.length()!=0) {
			return new MCPServerConfig("https://bedrock-agentcore." + AWS_REGION + ".amazonaws.com/runtimes/",
					AGENTCORE_RUNTIME_ID + "/invocations?qualifier=DEFAULT&accountId=" + this.getAccountId());			      
		} else if (AGENTCORE_GATEWAY_BASE_URL.length() !=0) {
			return new MCPServerConfig(AGENTCORE_GATEWAY_BASE_URL, AGENTCORE_GATEWAY_ENDPOINT);
		}
		else throw new RuntimeException(" no AgentCore Runtime Id or AgentCore Gateway URL defined");
	}
	
	
	private String getAccountId() {
	    var awsAccountId= stsClient.getCallerIdentity().account();
	    logger.info("AWS Account Id "+awsAccountId);
	    return awsAccountId;
	}
	
	
	/**
	 * returns authorization token required by the mcp client
	 * @return authorization token
	 */
	private String getAuthTokenViaHttpClient() {
		var userPool = getUserPool();
		logger.info("user pool " + userPool);
		if(userPool == null) {
			throw new RuntimeException("cognito user pool with the name "+USER_POOL_NAME+ " is not found");
		}
		var userPoolClient = getUserPoolClient(userPool);
		logger.info("user pool " + userPoolClient);
		
		if(userPoolClient == null) {
			throw new RuntimeException("cognito user pool client with the name "+USER_POOL_CLIENT_NAME+ " is not found");
		}

		var userPoolClientType = describeUserPoolClient(userPoolClient);
		logger.info("user pool client type " + userPoolClientType);
		
		if(userPoolClientType == null) {
			throw new RuntimeException("cognito user client type for the client "+USER_POOL_CLIENT_NAME+ " is not found");
		}
		var userPoolId = userPool.id();
		userPoolId = userPoolId.replace("_", "").toLowerCase();
		var url = "https://" + userPoolId + ".auth." + Region.US_EAST_1.id() + ".amazoncognito.com/oauth2/token";
		logger.info("url: " + url);

		String SCOPE_STRING = RESOURCE_SERVER_ID + "/*";
		
		String entity = "grant_type=client_credentials&" + "client_id=" + userPoolClientType.clientId() + "&"
				+ "client_secret=" + userPoolClientType.clientSecret() + "&" + "scope=" + SCOPE_STRING;

		logger.info("entity " + entity);
		try (var httpClient = HttpClients.createDefault()) {
			var httpPost = ClassicRequestBuilder.post(url)
					.setHeader("Content-Type", "application/x-www-form-urlencoded").setEntity(entity).build();
			return httpClient.execute(httpPost, new AuthTokenResponseHandler());
			
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("error occured with the message: ", e.getMessage());
		}
		return null;
	}

	
	/**
	 * returns cognito user pool with specific user name
	 * 
	 * @return cognito user pool with specific user name
	 */
	private UserPoolDescriptionType getUserPool() {
		try {
			var request = ListUserPoolsRequest.builder().maxResults(10).build();
			var response = cognitoClient.listUserPools(request);
			for (var userPool : response.userPools()) {
				logger.info("User pool " + userPool.name() + ", User ID " + userPool.id());
				if (userPool.name().equals(USER_POOL_NAME)) {
					return userPool;
				}
			}

		} catch (CognitoIdentityProviderException e) {
			logger.error("error occured with the message: ", e.getMessage());
		}
		return null;
	}

	/**
	 * returns cognito user pool client for the given cognito user pool
	 * 
	 * @param userPool - cognito user pool
	 * @return cognito user pool client for the given cognito user pool
	 */
	private UserPoolClientDescription getUserPoolClient(UserPoolDescriptionType userPool) {
		try {
			var request = ListUserPoolClientsRequest.builder().userPoolId(userPool.id()).maxResults(10).build();

			var response = cognitoClient.listUserPoolClients(request);
			for (var userPoolClient : response.userPoolClients()) {
				logger.info("User Pool Client Name " + userPoolClient.clientName() + ", User Pool Client ID "
						+ userPoolClient.clientId());
				if (userPoolClient.clientName().equals(USER_POOL_CLIENT_NAME)) {
					return userPoolClient;
				}
			}
		} catch (CognitoIdentityProviderException e) {
			logger.error("error occured with the message: ", e.getMessage());
		}
		return null;
	}

	

	/** returns cognito user pool client type for the given cognito user pool client
	 * 
	 * @param userPoolClient- cognito user pool client
	 * @return cognito user pool client type for the given cognito user pool client
	 */
	private static UserPoolClientType describeUserPoolClient(UserPoolClientDescription userPoolClient) {
		var request = DescribeUserPoolClientRequest.builder()
				.userPoolId(userPoolClient.userPoolId()).clientId(userPoolClient.clientId()).build();
		var response = cognitoClient.describeUserPoolClient(request);
		var optionalType = response.getValueForField("UserPoolClient",
				UserPoolClientType.class);
		if(optionalType.isEmpty()) {
			return null;
		}
		return optionalType.get();
	}
	
	private class AuthTokenResponseHandler implements HttpClientResponseHandler<String> {
		@Override
		public String handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
			var inputStream = response.getEntity().getContent();
			var responseString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			logger.info("response: " + responseString);

			var responseMap = objectMapper.readValue(responseString, new TypeReference<Map<String, Object>>() {});
			var token = (String) responseMap.get("access_token");
			logger.info("token : " + token);
			return token;
		}
	}
	
	private record MCPServerConfig(String baseUrl, String endpoint) {
	}
}