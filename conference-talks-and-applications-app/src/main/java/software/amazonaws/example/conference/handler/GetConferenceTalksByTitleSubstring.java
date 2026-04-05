package software.amazonaws.example.conference.handler;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import software.amazon.awssdk.http.HttpStatusCode;
import software.amazonaws.example.conference.entity.ConferenceTalk;

public class GetConferenceTalksByTitleSubstring
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final Logger logger = LoggerFactory.getLogger(GetConferenceTalksByTitleSubstring.class);
	
	private Set<ConferenceTalk> talks = Set.of(
				new ConferenceTalk(1,"Java on AWS Serverless","Description 1"),
				new ConferenceTalk(2,"Amazon Bedrock AgentCore with Java and Spring AI","Description 2"),
				new ConferenceTalk(2,"Amazon Bedrock AgentCore with Python and Strands Agents SDK","Description 3")
			);

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
		var titleSubstring = requestEvent.getPathParameters().get("titleSubstring");
		logger.info("title substring: "+titleSubstring);
		
		var matchedTalks=talks.stream()
				.filter(t -> t.title().contains(titleSubstring))
				.collect(Collectors.toSet());
	
		return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.OK)
						.withBody(" found matched talks: = " + matchedTalks );
	}

}