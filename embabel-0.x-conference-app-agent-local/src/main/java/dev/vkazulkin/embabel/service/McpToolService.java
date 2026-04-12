package dev.vkazulkin.embabel.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.WebClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import reactor.netty.http.client.HttpClient;
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


@Service
public class McpToolService {

	@Value("${cognito.user.pool.name}")
	private String USER_POOL_NAME;
	
	@Value("${cognito.user.pool.client.name}")
	private String USER_POOL_CLIENT_NAME;
	
	@Value("${cognito.auth.token.resource.server.id}")
	private String RESOURCE_SERVER_ID;

	@Value("${amazon.bedrock.agentcore.gateway.url}")
	private String AGENTCORE_GATEWAY_URL;
	
	@Value("${amazon.bedrock.agentcore.runtime.id}")
	private String AGENTCORE_RUNTIME_ID;

	private final String awsRegion;
	
	private final CognitoIdentityProviderClient cognitoClient;
			
	private final StsClient stsClient;
	  
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	private static final Logger logger = LoggerFactory.getLogger(McpToolService.class);

	public McpToolService(@Value("${aws.region}") String awsRegion) {
		this.awsRegion=awsRegion;
		cognitoClient = CognitoIdentityProviderClient.builder().region(Region.of(awsRegion)).build();
		stsClient = StsClient.builder().region(Region.of(awsRegion)).build();
	}

	
	/** returns mcp sync client
	 * 
	 * @return mcp sync client
	 */
	public McpSyncClient getMcpClient() {	
		String token = getAuthTokenViaHttpClient();
		return McpClient.sync(getMcpClientTransport(token)).build(); 
	}

	/**
	 * returns streamable http mcp client transport
	 * 
	 * @param token -bearer authorization token
	 * @return streamable http mcp client transport
	 */
	private McpClientTransport getMcpClientTransport(String token) {
		
		var MCP_SERVER_ENDPOINT= this.getMCPServerEndpoint();
		logger.info("MCP Server endpoint: " + MCP_SERVER_ENDPOINT);
		String headerValue = "Bearer " + token;
		
		 HttpClient httpClient = HttpClient.create()
				    .option(ChannelOption.SO_KEEPALIVE, true)
				    .option(EpollChannelOption.TCP_KEEPIDLE, 10)
				    .option(EpollChannelOption.TCP_KEEPINTVL, 5)
				    .option(EpollChannelOption.TCP_KEEPCNT, 8)
				    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000*300)
		            .responseTimeout(Duration.ofMinutes(5));
          
	
		var webClientBuilder = WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.defaultHeader("Authorization", headerValue)
				.defaultHeader("accept","application/json, text/event-stream")
		        .defaultHeader("Content-Type","application/json");
		
		return WebClientStreamableHttpTransport
				.builder(webClientBuilder)
				.endpoint(MCP_SERVER_ENDPOINT)
				.build();
	}

	
	private String getMCPServerEndpoint() {
		if(AGENTCORE_RUNTIME_ID.length()!=0) {
			return "https://bedrock-agentcore." + awsRegion + ".amazonaws.com/runtimes/"
			     + AGENTCORE_RUNTIME_ID + "/invocations?qualifier=DEFAULT&accountId=" + this.getAccountId();
		} else if (AGENTCORE_GATEWAY_URL.length() !=0) {
			return AGENTCORE_GATEWAY_URL;
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
	private UserPoolClientType describeUserPoolClient(UserPoolClientDescription userPoolClient) {
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
}