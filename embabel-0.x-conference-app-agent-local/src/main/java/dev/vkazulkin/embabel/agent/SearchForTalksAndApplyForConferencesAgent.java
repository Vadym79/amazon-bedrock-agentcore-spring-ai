package dev.vkazulkin.embabel.agent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmOptions;

import dev.vkazulkin.embabel.config.ConferenceConfig;
import dev.vkazulkin.embabel.domain.Domain;
import dev.vkazulkin.embabel.service.McpToolService;

@Agent(name=SearchForTalksAndApplyForConferencesAgent.AGENT_NAME , description = "search for the talk(s), search for the conference(s) by the given criteria (all, by the topic, by the date range and by cal lfor papers being open on some date), and apply for them with the found talks")
public class SearchForTalksAndApplyForConferencesAgent extends AbstractConferenceAgent {
	
	public static final String AGENT_NAME="SearchForExistingTalksAndApplyForConferencesAgent"; 

	private static final Logger logger = LoggerFactory.getLogger(SearchForTalksAndApplyForConferencesAgent.class);
	
	public SearchForTalksAndApplyForConferencesAgent(ConferenceConfig config, ToolGroup toolGroup, McpToolService mcpToolService) {
		super(config, toolGroup, mcpToolService);
	}

	
	@Action
	Domain.TalkSearchRequest extractTalkSearchRequest(UserInput userInput, OperationContext context) {
		logger.info("invoked talkSearchRequest with the request: "+userInput);
		return  context.ai()
	           //.withDefaultLlm()
			    .withLlm(LlmOptions.withModel("us.amazon.nova-pro-v1:0"))
	           .createObject("""
	                Create a talk search request from this user input, extracting the talk title substring. 
	                Don't include any other information into the request.:
	                %s""".formatted(userInput.getContent()), Domain.TalkSearchRequest.class);
	}	

		
	@Action
	Domain.Talks talkSearch(Domain.TalkSearchRequest talkSearchRequest, Ai ai) {
		logger.info("invoked talkSearch with the request: "+talkSearchRequest);
		return config.speaker()		
			.promptRunner(ai)
			.withPromptContributors(List.of(talkSearchRequest))
			.withToolGroup(this.toolGroup)
			.createObject("search for the talk with the given criteria", Domain.Talks.class);
	}	
}