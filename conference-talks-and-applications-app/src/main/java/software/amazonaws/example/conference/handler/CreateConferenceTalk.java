// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.conference.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.http.HttpStatusCode;
import software.amazonaws.example.conference.entity.ConferenceTalk;


public class CreateConferenceTalk implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(CreateConferenceTalk.class);

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {	
			try {
				var requestBody = requestEvent.getBody();
				logger.info("request body received: "+requestBody);
				var conferenceTalk = objectMapper.readValue(requestBody, ConferenceTalk.class);
				var talkId=this.getRandomTalkId(1, 100);
				var createdConferenceTalk= new ConferenceTalk(talkId, conferenceTalk.title(), conferenceTalk.description());
				logger.info("created conference talk: "+createdConferenceTalk);
				return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.CREATED)
						.withBody("application = " + createdConferenceTalk + " created");
			} catch (Exception e) {
				return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
						.withBody("Internal Server Error :: " + e.getMessage());
			} 
	}
	
	
	private int getRandomTalkId(int min, int max) {
	    return (int) ((Math.random() * (max - min)) + min);
	}
}